package com.example.kintai.repository;

import com.example.kintai.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findByUserIdAndWorkDate(UUID userId, LocalDate workDate);
    List<AttendanceRecord> findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(UUID userId, LocalDate from, LocalDate to);

    /** CSV出力用: 特定ユーザーの期間レコードをユーザー情報込みで取得 */
    @Query("SELECT r FROM AttendanceRecord r JOIN FETCH r.user WHERE r.user.id = :userId AND r.workDate BETWEEN :from AND :to ORDER BY r.workDate ASC")
    List<AttendanceRecord> findByUserIdAndWorkDateBetweenFetchUser(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** CSV出力用: 全ユーザーの期間レコードをユーザー情報込みで取得 */
    @Query("SELECT r FROM AttendanceRecord r JOIN FETCH r.user WHERE r.workDate BETWEEN :from AND :to ORDER BY r.user.loginId ASC, r.workDate ASC")
    List<AttendanceRecord> findAllByWorkDateBetweenFetchUser(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
