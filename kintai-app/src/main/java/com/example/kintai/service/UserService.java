package com.example.kintai.service;

import com.example.kintai.dto.request.UserCreateRequest;
import com.example.kintai.dto.request.UserUpdateRequest;
import com.example.kintai.dto.response.UserResponse;
import com.example.kintai.entity.User;
import com.example.kintai.entity.Role;
import com.example.kintai.repository.RoleRepository;
import com.example.kintai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** ユーザー管理サービス */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable).map(UserResponse::from);
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
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        var user = findOrThrow(id);
        user.setActive(false);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
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
