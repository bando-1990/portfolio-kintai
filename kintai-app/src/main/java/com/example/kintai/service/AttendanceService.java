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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** 勤怠打刻サービス */
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
}
