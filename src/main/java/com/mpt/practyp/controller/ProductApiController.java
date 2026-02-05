package com.mpt.practyp.controller;

import com.mpt.practyp.model.Product;
import com.mpt.practyp.service.ProductService;
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
@RequestMapping("/api/products")
@Tag(name = "Товары", description = "API для управления товарами")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Получить все товары")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Product>>> getAllProducts() {
        return productService.findAllAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить товар по ID")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Product>> getProductById(@PathVariable Long id) {
        return productService.findByIdAsync(id)
                .thenApply(productOpt -> productOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Создать новый товар")
    @PostMapping
    public CompletableFuture<ResponseEntity<Product>> createProduct(@Valid @RequestBody Product product) {
        return productService.saveAsync(product)
                .thenApply(savedProduct -> ResponseEntity.status(HttpStatus.CREATED).body(savedProduct))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Operation(summary = "Обновить товар")
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {

        return productService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Product>notFound().build());
                    }
                    product.setId(id);
                    return productService.saveAsync(product)
                            .thenApply(ResponseEntity::ok);
                });
    }

    @Operation(summary = "Удалить товар")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteProduct(@PathVariable Long id) {
        return productService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Void>notFound().build());
                    }
                    return productService.deleteByIdAsync(id)
                            .thenApply(v -> ResponseEntity.noContent().<Void>build());
                });
    }

    @Operation(summary = "Поиск товаров")
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<List<Product>>> searchProducts(@RequestParam String name) {
        return productService.searchByNameAsync(name)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить товары с пагинацией")
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<Page<Product>>> getProductsPage(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return productService.findAllAsync(pageable, includeDeleted)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}