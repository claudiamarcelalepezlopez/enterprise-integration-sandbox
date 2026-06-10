package com.integration.orderapi.service;

import com.integration.orderapi.dto.OrderDtos.*;
import com.integration.orderapi.dto.OrderMapper;
import com.integration.orderapi.exception.OrderExceptions.*;
import com.integration.orderapi.model.Order;
import com.integration.orderapi.model.Order.OrderStatus;
import com.integration.orderapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * OrderService — all business logic lives here, NOT in the controller.
 *
 * Key patterns used:
 * ─ @Transactional(readOnly = true) on reads  → DB-level performance hint
 * ─ Switch expressions (Java 14+)             → exhaustive status validation
 * ─ Sealed transition map                     → business rules are explicit & testable
 * ─ @RequiredArgsConstructor                  → constructor injection, no @Autowired
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // default for all methods; overridden on writes
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper      orderMapper;

    // ─── Valid status transitions (business rule as data) ────────────────────
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.PENDING,     EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
        OrderStatus.CONFIRMED,   EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
        OrderStatus.PROCESSING,  EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED,     EnumSet.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED,   EnumSet.noneOf(OrderStatus.class),
        OrderStatus.CANCELLED,   EnumSet.noneOf(OrderStatus.class)
    );

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customerId={}", request.customerId());

        Order order = orderMapper.toEntity(request);
        Order saved = orderRepository.save(order);

        log.info("Order created: id={}, customerId={}", saved.getId(), saved.getCustomerId());
        return orderMapper.toResponse(saved);
    }

    // ─── READ ────────────────────────────────────────────────────────────────

    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
            .map(orderMapper::toResponse)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public PagedResponse<OrderResponse> getAllOrders(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Order> result = orderRepository.findAll(pageable);
        return toPagedResponse(result);
    }

    public PagedResponse<OrderResponse> getOrdersByCustomer(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> result = orderRepository.findByCustomerId(customerId, pageable);
        return toPagedResponse(result);
    }

    public PagedResponse<OrderResponse> getOrdersByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> result = orderRepository.findByStatus(status, pageable);
        return toPagedResponse(result);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        validateTransition(order.getStatus(), request.status());

        log.info("Order id={} status transition: {} → {}", id, order.getStatus(), request.status());
        orderMapper.updateStatusFromRequest(request, order);

        // No explicit save() needed — dirty-checking within @Transactional persists changes
        return orderMapper.toResponse(order);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        validateTransition(order.getStatus(), OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);
        log.info("Order id={} cancelled", id);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validateTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(next)) {
            throw new InvalidStatusTransitionException(current.name(), next.name());
        }
    }

    private PagedResponse<OrderResponse> toPagedResponse(Page<Order> page) {
        return new PagedResponse<>(
            page.getContent().stream().map(orderMapper::toResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
