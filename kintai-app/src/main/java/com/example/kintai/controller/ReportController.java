package com.example.kintai.controller;

import com.example.kintai.dto.response.DailySummaryResponse;
import com.example.kintai.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** 集計レポートエンドポイント */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary/daily")
    public ResponseEntity<List<DailySummaryResponse>> getDailySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        UUID requesterId = UUID.fromString(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // ADMIN のみ任意ユーザーのデータを取得可能
        UUID targetId = (isAdmin && userId != null) ? userId : requesterId;
        return ResponseEntity.ok(reportService.getDailySummary(targetId, from, to));
    }
}
