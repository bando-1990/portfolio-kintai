package com.example.kintai.controller;

import com.example.kintai.dto.request.ForgotPasswordRequest;
import com.example.kintai.dto.request.ResetPasswordRequest;
import com.example.kintai.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** パスワードリセットエンドポイント（認証不要） */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /** リセットメール送信 */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        // メールアドレスの存在有無を外部に漏らさないため常に204を返す
        return ResponseEntity.noContent().build();
    }

    /** 新パスワード設定 */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
