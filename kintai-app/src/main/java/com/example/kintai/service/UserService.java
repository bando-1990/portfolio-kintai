package com.example.kintai.service;

import com.example.kintai.dto.request.UpdateMyProfileRequest;
import com.example.kintai.dto.request.UserCreateRequest;
import com.example.kintai.dto.request.UserUpdateRequest;
import com.example.kintai.dto.response.CsvImportResultResponse;
import com.example.kintai.dto.response.UserResponse;
import com.example.kintai.entity.User;
import com.example.kintai.entity.Role;
import com.example.kintai.repository.RoleRepository;
import com.example.kintai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** ユーザー管理サービス */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAllActiveUsers() {
        return userRepository.findAllByActiveTrueOrderByLoginIdAsc()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        return UserResponse.from(findOrThrow(id));
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "login_id が重複しています");
        }
        var now = OffsetDateTime.now();
        var user = User.builder()
                .id(UUID.randomUUID())
                .loginId(request.loginId())
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .active(true)
                .roles(resolveRoles(request.roles()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        log.info("ユーザー作成: loginId={}, roles={}", request.loginId(), request.roles());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        var user = findOrThrow(id);
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) user.setEmail(request.email());
        if (request.active() != null) user.setActive(request.active());
        if (request.roles() != null) user.setRoles(resolveRoles(request.roles()));
        user.setUpdatedAt(OffsetDateTime.now());
        log.info("ユーザー更新: id={}, active={}", id, user.getActive());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        var user = findOrThrow(userId);

        // パスワード変更
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "現在のパスワードを入力してください");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "現在のパスワードが正しくありません");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        // メールアドレス更新（空文字列は削除、nullは変更なし）
        if (request.email() != null) {
            String newEmail = request.email().isBlank() ? null : request.email();
            if (newEmail != null && userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "このメールアドレスは既に他のユーザーが使用しています");
            }
            user.setEmail(newEmail);
        }

        user.setUpdatedAt(OffsetDateTime.now());
        log.info("プロフィール更新: userId={}, emailUpdated={}, passwordChanged={}",
                userId,
                request.email() != null,
                request.newPassword() != null && !request.newPassword().isBlank());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        var user = findOrThrow(id);
        try {
            userRepository.deleteById(id);
            userRepository.flush();
            log.info("ユーザー削除: id={}, loginId={}", id, user.getLoginId());
        } catch (DataIntegrityViolationException e) {
            log.warn("ユーザー削除失敗（勤怠データあり）: id={}, loginId={}", id, user.getLoginId());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "このユーザーには勤怠データが存在するため削除できません");
        }
    }

    @Transactional
    public CsvImportResultResponse importFromCsv(MultipartFile file) throws IOException {
        int created = 0, skipped = 0, errors = 0;
        List<String> errorDetails = new ArrayList<>();

        var format = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(true) // 1行目のヘッダーをスキップ
                .setTrim(true)
                .build();

        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             var parser = format.parse(reader)) {

            for (var record : parser) {
                long lineNo = record.getRecordNumber() + 1;
                try {
                    if (record.size() < 2) {
                        errors++;
                        errorDetails.add(lineNo + "行目: 列数が不足しています");
                        continue;
                    }
                    String loginId = record.get(0); // 社員ID
                    String name    = record.get(1); // 氏名

                    if (loginId.isBlank() || name.isBlank()) {
                        errors++;
                        errorDetails.add(lineNo + "行目: 社員IDまたは氏名が空です");
                        continue;
                    }
                    if (userRepository.existsByLoginId(loginId)) {
                        skipped++;
                        continue;
                    }

                    var now = OffsetDateTime.now();
                    var user = User.builder()
                            .id(UUID.randomUUID())
                            .loginId(loginId)
                            .name(name)
                            .passwordHash(passwordEncoder.encode(loginId)) // 初期パスワード = 社員ID
                            .active(true)
                            .roles(resolveRoles(List.of("MEMBER")))
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    userRepository.save(user);
                    created++;
                } catch (Exception e) {
                    errors++;
                    errorDetails.add(lineNo + "行目: " + e.getMessage());
                }
            }
        }
        log.info("CSVインポート完了: created={}, skipped={}, errors={}", created, skipped, errors);
        return new CsvImportResultResponse(created, skipped, errors, errorDetails);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    }

    private Set<Role> resolveRoles(List<String> codes) {
        if (codes == null) return new HashSet<>();
        return codes.stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "不明なロール: " + code)))
                .collect(Collectors.toSet());
    }
}
