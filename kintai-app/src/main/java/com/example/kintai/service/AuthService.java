package com.example.kintai.service;

import com.example.kintai.dto.request.LoginRequest;
import com.example.kintai.dto.response.LoginResponse;
import com.example.kintai.repository.UserRepository;
import com.example.kintai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 認証サービス */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            var user = userRepository.findByLoginId(request.loginId())
                    .filter(u -> u.getActive())
                    .orElseThrow(() -> new BadCredentialsException("認証情報が正しくありません"));

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new BadCredentialsException("認証情報が正しくありません");
            }

            String token = jwtTokenProvider.generateToken(user.getId(), user.getLoginId());
            log.info("ログイン成功: loginId={}", request.loginId());
            return new LoginResponse(token, jwtTokenProvider.getExpirationMs() / 1000);

        } catch (BadCredentialsException e) {
            log.warn("ログイン失敗: loginId={}", request.loginId());
            throw e;
        }
    }
}
