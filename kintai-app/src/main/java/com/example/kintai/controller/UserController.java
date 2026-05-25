package com.example.kintai.controller;

import com.example.kintai.dto.request.UpdateMyProfileRequest;
import com.example.kintai.dto.request.UserCreateRequest;
import com.example.kintai.dto.request.UserUpdateRequest;
import com.example.kintai.dto.response.CsvImportResultResponse;
import com.example.kintai.dto.response.UserResponse;
import com.example.kintai.service.UserService;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/** ユーザー管理エンドポイント */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    /** ページネーションなし全件取得（ドロップダウン用） */
    @GetMapping("/users/all")
    public ResponseEntity<List<UserResponse>> listAllUsers() {
        return ResponseEntity.ok(userService.listAllActiveUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        var created = userService.createUser(request);
        return ResponseEntity.created(URI.create("/users/" + created.id())).body(created);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/import/csv")
    public ResponseEntity<CsvImportResultResponse> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userService.importFromCsv(file));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUser(UUID.fromString(userDetails.getUsername())));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateMyProfileRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(userService.updateMyProfile(userId, request));
    }
}
