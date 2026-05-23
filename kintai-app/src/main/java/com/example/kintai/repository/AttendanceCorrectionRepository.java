package com.example.kintai.repository;

import com.example.kintai.entity.AttendanceCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, UUID> {

    // 申請者として一覧取得
    List<AttendanceCorrection> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId);

    // 承認者として一覧取得
    List<AttendanceCorrection> findByApproverIdOrderByCreatedAtDesc(UUID approverId);

    // ステータスで絞り込み
    @Query("SELECT c FROM AttendanceCorrection c WHERE c.applicant.id = :userId AND c.status = :status ORDER BY c.createdAt DESC")
    List<AttendanceCorrection> findByApplicantIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT c FROM AttendanceCorrection c WHERE c.approver.id = :userId AND c.status = :status ORDER BY c.createdAt DESC")
    List<AttendanceCorrection> findByApproverIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
}
