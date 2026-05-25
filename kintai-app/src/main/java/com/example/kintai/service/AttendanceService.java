package com.example.kintai.service;

import com.example.kintai.dto.request.ClockInRequest;
import com.example.kintai.dto.request.ClockOutRequest;
import com.example.kintai.dto.response.AttendanceRecordResponse;
import com.example.kintai.entity.AttendanceRecord;
import com.example.kintai.entity.AttendanceStatus;
import com.example.kintai.entity.IdempotencyKey;
import com.example.kintai.repository.AttendanceRecordRepository;
import com.example.kintai.repository.IdempotencyKeyRepository;
import com.example.kintai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/** 勤怠打刻サービス */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository recordRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final UserRepository userRepository;

    @Transactional
    public AttendanceRecordResponse clockIn(UUID userId, String idempotencyKey, ClockInRequest request) {
        var workDate = request.workDate() != null ? request.workDate() : LocalDate.now();

        // 冪等性チェック: 同じキーが既に存在する場合は既存レコードを返す
        if (idempotencyKeyRepository.existsById(idempotencyKey)) {
            return recordRepository.findByUserIdAndWorkDate(userId, workDate)
                    .map(AttendanceRecordResponse::from)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "冪等性キーの不整合"));
        }

        if (recordRepository.findByUserIdAndWorkDate(userId, workDate).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "本日は既に出勤打刻済みです");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        var now = OffsetDateTime.now();
        var record = AttendanceRecord.builder()
                .id(UUID.randomUUID())
                .user(user)
                .workDate(workDate)
                .clockInAt(now)
                .status(AttendanceStatus.WORKING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        recordRepository.save(record);

        idempotencyKeyRepository.save(IdempotencyKey.builder()
                .key(idempotencyKey)
                .userId(userId)
                .requestHash(idempotencyKey)
                .createdAt(now)
                .build());

        log.info("出勤打刻: userId={}, workDate={}", userId, workDate);
        return AttendanceRecordResponse.from(record);
    }

    @Transactional
    public AttendanceRecordResponse clockOut(UUID userId, String idempotencyKey, ClockOutRequest request) {
        var workDate = request.workDate() != null ? request.workDate() : LocalDate.now();
        var record = recordRepository.findByUserIdAndWorkDate(userId, workDate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "出勤レコードが見つかりません"));

        if (record.getStatus() != AttendanceStatus.WORKING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "打刻状態が不正です: " + record.getStatus());
        }

        var now = OffsetDateTime.now();
        long grossMinutes = Duration.between(record.getClockInAt(), now).toMinutes();
        // 6時間以上で45分、8時間以上で60分の自動休憩を計上
        int autoBreak = grossMinutes >= 480 ? 60 : grossMinutes >= 360 ? 45 : 0;

        record.setClockOutAt(now);
        record.setStatus(AttendanceStatus.COMPLETED);
        record.setGrossWorkMinutes((int) grossMinutes);
        record.setAutoBreakMinutes(autoBreak);
        record.setNetWorkMinutes((int) grossMinutes - autoBreak);
        record.setCalcAt(now);
        record.setUpdatedAt(now);

        log.info("退勤打刻: userId={}, workDate={}, 実働={}分", userId, workDate, record.getNetWorkMinutes());
        return AttendanceRecordResponse.from(recordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getRecords(UUID userId, LocalDate from, LocalDate to) {
        return recordRepository
                .findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(userId, from, to)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }

    /**
     * 勤怠レコードをUTF-8 BOM付きCSVとして出力する。
     * isAdmin=true かつ targetUserId=null の場合は全ユーザー分を出力。
     */
    @Transactional(readOnly = true)
    public byte[] exportCsv(UUID requesterId, boolean isAdmin, Integer year, Integer month, UUID targetUserId) {
        LocalDate from;
        LocalDate to;
        if (month != null) {
            from = LocalDate.of(year, month, 1);
            to = from.withDayOfMonth(from.lengthOfMonth());
        } else {
            from = LocalDate.of(year, 1, 1);
            to = LocalDate.of(year, 12, 31);
        }

        List<AttendanceRecord> records;
        if (isAdmin && targetUserId == null) {
            records = recordRepository.findAllByWorkDateBetweenFetchUser(from, to);
        } else {
            UUID uid = (isAdmin && targetUserId != null) ? targetUserId : requesterId;
            records = recordRepository.findByUserIdAndWorkDateBetweenFetchUser(uid, from, to);
        }

        var sb = new StringBuilder();
        sb.append('﻿'); // UTF-8 BOM（Excel での文字化け防止）
        sb.append("社員ID,氏名,日付,出勤,退勤,総労働(h),休憩控除(h),実働(h),状態\r\n");

        for (var r : records) {
            sb.append(escapeCsv(r.getUser().getLoginId())).append(',');
            sb.append(escapeCsv(r.getUser().getName())).append(',');
            sb.append(r.getWorkDate()).append(',');
            sb.append(fmtTimeCsv(r.getClockInAt())).append(',');
            sb.append(fmtTimeCsv(r.getClockOutAt())).append(',');
            sb.append(minutesToDecimalHours(r.getGrossWorkMinutes())).append(',');
            sb.append(minutesToDecimalHours(r.getAutoBreakMinutes())).append(',');
            sb.append(minutesToDecimalHours(r.getNetWorkMinutes())).append(',');
            sb.append(statusLabel(r.getStatus())).append("\r\n");
        }

        log.info("CSV出力: userId={}, year={}, month={}, targetUserId={}, 件数={}",
                requesterId, year, month, targetUserId, records.size());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String fmtTimeCsv(OffsetDateTime dt) {
        if (dt == null) return "";
        return dt.atZoneSameInstant(ZoneId.of("Asia/Tokyo"))
                 .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String minutesToDecimalHours(Integer minutes) {
        if (minutes == null) return "";
        return String.format("%.2f", minutes / 60.0);
    }

    private String statusLabel(AttendanceStatus status) {
        if (status == null) return "";
        return switch (status) {
            case NOT_STARTED -> "未出勤";
            case WORKING     -> "出勤中";
            case COMPLETED   -> "退勤済";
        };
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
