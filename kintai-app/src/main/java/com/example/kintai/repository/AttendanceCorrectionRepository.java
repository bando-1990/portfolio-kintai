package com.example.kintai.repository;

import com.example.kintai.entity.AttendanceCorrection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, UUID> {

    // 申請者として一覧取得
    Page<AttendanceCorrection> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId, Pageable pageable);

    // ステータスで絞り込み（申請者）
    @Query("SELECT c FROM AttendanceCorrection c WHERE c.applicant.id = :userId AND c.status = :status ORDER BY c.createdAt DESC")
    Page<AttendanceCorrection> findByApplicantIdAndStatus(@Param("userId") UUID userId, @Param("status") String status, Pageable pageable);

    // 承認者向け：全件（pendingは approver 未設定のため全件クエリ）
    Page<AttendanceCorrection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 承認者向け：ステータスで絞り込み
    Page<AttendanceCorrection> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
