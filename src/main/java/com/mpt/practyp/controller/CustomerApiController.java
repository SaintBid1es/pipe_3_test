package com.mpt.practyp.controller;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Клиенты", description = "API для управления пользователями")
public class CustomerApiController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "Все пользователи")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Customer>>> getAllCustomers() {
        return customerService.findAllAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Поиск клиента по ID")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Customer>> getCustomerById(@PathVariable Long id) {
        return customerService.findByIdAsync(id)
                .thenApply(customerOpt -> customerOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Создать нового клиента")
    @PostMapping
    public CompletableFuture<ResponseEntity<Customer>> createCustomer(@Valid @RequestBody Customer customer) {
        return customerService.saveAsync(customer)
                .thenApply(savedCustomer -> ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Operation(summary = "Обновить клиента")
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Customer>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer) {

        return customerService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Customer>notFound().build());
                    }
                    customer.setId(id);
                    return customerService.saveAsync(customer)
                            .thenApply(ResponseEntity::ok);
                });
    }

    @Operation(summary = "Удалить клиента")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteCustomer(@PathVariable Long id) {
        return customerService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Void>notFound().build());
                    }
                    return customerService.deleteByIdAsync(id)
                            .thenApply(v -> ResponseEntity.noContent().<Void>build());
                });
    }

    @Operation(summary = "Поиск клиентов")
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<List<Customer>>> searchCustomers(@RequestParam String query) {
        return customerService.searchAsync(query)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить клиентов с пагинацией")
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<Page<Customer>>> getCustomersPage(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return customerService.findAllAsync(includeDeleted, pageable)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}