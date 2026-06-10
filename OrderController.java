package com.integration.orderapi.controller;

import com.integration.orderapi.dto.OrderDtos.*;
import com.integration.orderapi.model.Order.OrderStatus;
import com.integration.orderapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * OrderController — HTTP surface only. Zero business logic here.
 *
 * Design principles:
 * ─ Thin controller: delegates everything to OrderService
 * ─ Returns 201 Created with Location header on POST (REST best practice)
 * ─ Uses ProblemDetail (RFC 7807) for errors via GlobalExceptionHandler
 * ─ @Validated enables constraint annotations on query params
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders", description = "Order processing endpoints")
public class OrderController {

    private final OrderService orderService;

    // ─── POST /api/v1/orders ─────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse created = orderService.createOrder(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    // ─── GET /api/v1/orders ──────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all orders (paginated)")
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0")  @Min(0)           int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt")            String sortBy) {

        return ResponseEntity.ok(orderService.getAllOrders(page, size, sortBy));
    }

    // ─── GET /api/v1/orders/{id} ─────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a single order by ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ─── GET /api/v1/orders/customer/{customerId} ────────────────────────────

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID")
    public ResponseEntity<PagedResponse<OrderResponse>> getOrdersByCustomer(
            @PathVariable @NotBlank String customerId,
            @RequestParam(defaultValue = "0")  @Min(0)           int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId, page, size));
    }

    // ─── GET /api/v1/orders/status/{status} ──────────────────────────────────

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<PagedResponse<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0")  @Min(0)           int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(orderService.getOrdersByStatus(status, page, size));
    }

    // ─── PATCH /api/v1/orders/{id}/status ───────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (validates allowed transitions)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    // ─── DELETE /api/v1/orders/{id} ──────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<Void> cancelOrder(@PathVariable @Positive Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
