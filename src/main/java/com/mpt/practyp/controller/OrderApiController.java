package com.mpt.practyp.controller;

import com.mpt.practyp.model.Order;
import com.mpt.practyp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Заказы", description = "API для управления заказами")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Получить все заказы")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Order>>> getAllOrders(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(pageNum, size);
        return orderService.findAllAsync(false, pageable)
                .thenApply(page -> ResponseEntity.ok(page.getContent()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить заказ по ID")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Order>> getOrderById(@PathVariable Long id) {
        return orderService.findByIdAsync(id)
                .thenApply(orderOpt -> orderOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Создать новый заказ")
    @PostMapping
    public CompletableFuture<ResponseEntity<Order>> createOrder(@Valid @RequestBody Order order) {
        return orderService.saveAsync(order)
                .thenApply(savedOrder -> ResponseEntity.status(HttpStatus.CREATED).body(savedOrder))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Operation(summary = "Обновить заказ")
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Order>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody Order order) {

        return orderService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Order>notFound().build());
                    }
                    order.setId(id);
                    return orderService.saveAsync(order)
                            .thenApply(ResponseEntity::ok);
                });
    }

    @Operation(summary = "Удалить заказ")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteOrder(@PathVariable Long id) {
        return orderService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Void>notFound().build());
                    }
                    return orderService.deleteByIdAsync(id)
                            .thenApply(v -> ResponseEntity.noContent().<Void>build());
                });
    }

    @Operation(summary = "Получить заказы клиента")
    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<List<Order>>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(pageNum, size);
        return orderService.findByCustomerIdAsync(customerId, pageable)
                .thenApply(page -> ResponseEntity.ok(page.getContent()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить заказы с пагинацией")
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<Page<Order>>> getOrdersPage(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return orderService.findAllAsync(includeDeleted, pageable)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}