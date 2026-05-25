package com.example.kintai.repository;

import com.example.kintai.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    // 同一ユーザーの未使用トークンを削除（再申請時のクリーンアップ）
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false")
    void deleteUnusedByUserId(@Param("userId") UUID userId);
}
