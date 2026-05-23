package com.example.kintai.controller;

import com.example.kintai.dto.request.ClockInRequest;
import com.example.kintai.dto.request.ClockOutRequest;
import com.example.kintai.dto.response.AttendanceRecordResponse;
import com.example.kintai.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** 勤怠打刻エンドポイント */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordResponse> clockIn(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody(required = false) ClockInRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        var body = request != null ? request : new ClockInRequest(null, null, null);
        return ResponseEntity.ok(attendanceService.clockIn(userId, idempotencyKey, body));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordResponse> clockOut(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody(required = false) ClockOutRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        var body = request != null ? request : new ClockOutRequest(null);
        return ResponseEntity.ok(attendanceService.clockOut(userId, idempotencyKey, body));
    }

    @GetMapping("/records")
    public ResponseEntity<List<AttendanceRecordResponse>> getRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(attendanceService.getRecords(userId, from, to));
    }
}
