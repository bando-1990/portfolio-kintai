package com.example.kintai.repository;

import com.example.kintai.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    Page<User> findByActiveTrue(Pageable pageable);
}
