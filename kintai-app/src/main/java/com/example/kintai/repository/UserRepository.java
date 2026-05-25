package com.example.kintai.repository;

import com.example.kintai.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Page<User> findByActiveTrue(Pageable pageable);
    List<User> findAllByActiveTrueOrderByLoginIdAsc();
}
