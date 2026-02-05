package com.mpt.practyp.controller;

import com.mpt.practyp.model.Tag;
import com.mpt.practyp.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Теги", description = "API для управления тегами товаров")
public class TagApiController {

    @Autowired
    private TagService tagService;

    @Operation(summary = "Получить все теги")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Tag>>> getAllTags() {
        return tagService.findAllAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить тег по ID")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Tag>> getTagById(@PathVariable Long id) {
        return tagService.findByIdAsync(id)
                .thenApply(tagOpt -> tagOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Создать новый тег")
    @PostMapping
    public CompletableFuture<ResponseEntity<Tag>> createTag(@Valid @RequestBody Tag tag) {
        return tagService.saveAsync(tag)
                .thenApply(savedTag -> ResponseEntity.status(HttpStatus.CREATED).body(savedTag))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Operation(summary = "Обновить тег")
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Tag>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody Tag tag) {

        return tagService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Tag>notFound().build());
                    }
                    tag.setId(id);
                    return tagService.saveAsync(tag)
                            .thenApply(ResponseEntity::ok);
                });
    }

    @Operation(summary = "Удалить тег")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteTag(@PathVariable Long id) {
        return tagService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Void>notFound().build());
                    }
                    return tagService.deleteByIdAsync(id)
                            .thenApply(v -> ResponseEntity.noContent().<Void>build());
                });
    }

    @Operation(summary = "Получить теги с пагинацией")
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<Page<Tag>>> getTagsPage(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return tagService.findAllAsync(includeDeleted, pageable)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}