package com.example.kintai.dto.response;

import com.example.kintai.entity.AttendanceRecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendanceRecordResponse(
    UUID id,
    UUID userId,
    LocalDate workDate,
    OffsetDateTime clockInAt,
    OffsetDateTime clockOutAt,
    String status,
    Integer netWorkMinutes
) {
    public static AttendanceRecordResponse from(AttendanceRecord r) {
        return new AttendanceRecordResponse(
            r.getId(),
            r.getUser().getId(),
            r.getWorkDate(),
            r.getClockInAt(),
            r.getClockOutAt(),
            r.getStatus().name(),
            r.getNetWorkMinutes()
        );
    }
}
