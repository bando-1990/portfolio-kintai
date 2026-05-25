package com.example.kintai.service;

import com.example.kintai.dto.request.ForgotPasswordRequest;
import com.example.kintai.dto.request.ResetPasswordRequest;
import com.example.kintai.entity.PasswordResetToken;
import com.example.kintai.repository.PasswordResetTokenRepository;
import com.example.kintai.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

/** パスワードリセットサービス */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    // トークン有効期限（1時間）
    private static final long TOKEN_EXPIRY_MINUTES = 60;

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        var userOpt = userRepository.findByEmail(request.email());

        // メールアドレスが存在しない場合もエラーを返さない（セキュリティ対策）
        if (userOpt.isEmpty()) {
            log.info("パスワードリセット要求: 存在しないメールアドレス {}", request.email());
            return;
        }

        var user = userOpt.get();

        // 既存の未使用トークンを削除
        tokenRepository.deleteUnusedByUserId(user.getId());

        // 新しいトークンを生成
        var rawToken = UUID.randomUUID().toString().replace("-", "");
        var resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .token(rawToken)
                .user(user)
                .expiresAt(OffsetDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .used(false)
                .createdAt(OffsetDateTime.now())
                .build();
        tokenRepository.save(resetToken);

        // リセットメールを送信
        sendResetEmail(user.getEmail(), user.getName(), rawToken);
        log.info("パスワードリセットメールを送信しました: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "無効なリセットトークンです"));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "このリセットリンクは既に使用済みです");
        }
        if (OffsetDateTime.now().isAfter(resetToken.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "リセットリンクの有効期限が切れています。再度お申し込みください");
        }

        // パスワードを更新
        var user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        // トークンを使用済みにする
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("パスワードをリセットしました: ユーザー {}", user.getLoginId());
    }

    private void sendResetEmail(String to, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("【勤怠管理システム】パスワードリセットのご案内");

            String resetUrl = frontendUrl + "/?view=reset&token=" + token;
            String html = """
                    <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                      <h2 style="color: #1e293b;">パスワードリセットのご案内</h2>
                      <p>%s 様</p>
                      <p>パスワードリセットのリクエストを受け付けました。<br>
                         下記のボタンをクリックして新しいパスワードを設定してください。</p>
                      <p style="margin: 24px 0;">
                        <a href="%s"
                           style="background:#3b82f6;color:#fff;padding:12px 24px;
                                  border-radius:8px;text-decoration:none;font-weight:bold;">
                          パスワードをリセットする
                        </a>
                      </p>
                      <p style="color:#6b7280;font-size:13px;">
                        このリンクの有効期限は <strong>1時間</strong> です。<br>
                        心当たりがない場合はこのメールを無視してください。
                      </p>
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0;">
                      <p style="color:#9ca3af;font-size:12px;">勤怠管理システム</p>
                    </div>
                    """.formatted(name, resetUrl);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("パスワードリセットメールの送信に失敗しました: {}", to, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "メールの送信に失敗しました");
        }
    }
}
