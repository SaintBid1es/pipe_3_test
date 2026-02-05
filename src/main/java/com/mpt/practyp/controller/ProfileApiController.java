package com.mpt.practyp.controller;

import com.mpt.practyp.model.Profile;
import com.mpt.practyp.service.ProfileService;
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
@RequestMapping("/api/profiles")
@Tag(name = "Профили", description = "API для управления профилями клиентов")
public class ProfileApiController {

    @Autowired
    private ProfileService profileService;

    @Operation(summary = "Получить все профили")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Profile>>> getAllProfiles() {
        return profileService.findAllAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить профиль по ID")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Profile>> getProfileById(@PathVariable Long id) {
        return profileService.findByIdAsync(id)
                .thenApply(profileOpt -> profileOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Создать новый профиль")
    @PostMapping
    public CompletableFuture<ResponseEntity<Profile>> createProfile(@Valid @RequestBody Profile profile) {
        return profileService.saveAsync(profile)
                .thenApply(savedProfile -> ResponseEntity.status(HttpStatus.CREATED).body(savedProfile))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Operation(summary = "Обновить профиль")
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Profile>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody Profile profile) {

        return profileService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Profile>notFound().build());
                    }
                    profile.setId(id);
                    return profileService.saveAsync(profile)
                            .thenApply(ResponseEntity::ok);
                });
    }

    @Operation(summary = "Удалить профиль")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteProfile(@PathVariable Long id) {
        return profileService.existsByIdAsync(id)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(ResponseEntity.<Void>notFound().build());
                    }
                    return profileService.deleteByIdAsync(id)
                            .thenApply(v -> ResponseEntity.noContent().<Void>build());
                });
    }

    @Operation(summary = "Получить профиль по ID клиента")
    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<Profile>> getProfileByCustomerId(@PathVariable Long customerId) {
        return profileService.findByCustomerIdAsync(customerId)
                .thenApply(profileOpt -> profileOpt
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получить профили с пагинацией")
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<Page<Profile>>> getProfilesPage(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        return profileService.findAllAsync(includeDeleted, pageable)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}