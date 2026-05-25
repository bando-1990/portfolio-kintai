package com.example.kintai.controller;

import com.example.kintai.dto.request.ClockInRequest;
import com.example.kintai.dto.request.ClockOutRequest;
import com.example.kintai.dto.response.AttendanceRecordResponse;
import com.example.kintai.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
            @RequestParam(required = false) UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID requesterId = UUID.fromString(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID targetId = (isAdmin && userId != null) ? userId : requesterId;
        return ResponseEntity.ok(attendanceService.getRecords(targetId, from, to));
    }

    /** CSV出力: 管理者はuserIdなしで全員分、指定ありで個人分。一般ユーザーは常に自分のみ */
    @GetMapping("/records/export")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) UUID userId) {
        UUID requesterId = UUID.fromString(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        byte[] csv = attendanceService.exportCsv(requesterId, isAdmin, year, month, userId);

        String filename = month != null
                ? String.format("kintai_%d_%02d.csv", year, month)
                : String.format("kintai_%d.csv", year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
