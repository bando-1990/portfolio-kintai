package com.example.kintai.controller;

import com.example.kintai.dto.request.LoginRequest;
import com.example.kintai.dto.response.LoginResponse;
import com.example.kintai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 認証エンドポイント */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWTはステートレスのためクライアント側でトークンを破棄する
        return ResponseEntity.noContent().build();
    }
}
