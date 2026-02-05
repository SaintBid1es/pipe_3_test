package com.mpt.practyp.controller;

import com.mpt.practyp.model.Category;
import com.mpt.practyp.service.CategoryService;
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
@RequestMapping("/api/categories")
@Tag(name = "Категории", description = "API для управления категориями товаров")
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Получить все категории")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Category>>> getAllCategories() {
        return categoryService.findAllAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить категорию по ID")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Category>> getCategoryById(@PathVariable Long id) {
        return categoryService.findByIdAsync(id)
                .thenApply(categoryOpt -> categoryOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Создать новую категорию")
    @PostMapping
    public CompletableFuture<ResponseEntity<Category>> createCategory(@Valid @RequestBody Category category) {
        return categoryService.saveAsync(category)
                .thenApply(savedCategory -> ResponseEntity.status(HttpStatus.CREATED).body(savedCategory))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Operation(summary = "Обновить категорию")
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Category>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category) {

        return categoryService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Category>notFound().build());
                    }
                    category.setId(id);
                    return categoryService.saveAsync(category)
                            .thenApply(ResponseEntity::ok);
                });
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteCategory(@PathVariable Long id) {
        return categoryService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Void>notFound().build());
                    }
                    return categoryService.deleteByIdAsync(id)
                            .thenApply(v -> ResponseEntity.noContent().<Void>build());
                });
    }

    @Operation(summary = "Получить категории с пагинацией")
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<Page<Category>>> getCategoriesPage(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return categoryService.findAllAsync(includeDeleted, pageable)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}