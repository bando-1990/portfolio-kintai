package com.example.kintai.controller;

import com.example.kintai.dto.request.ApprovalActionRequest;
import com.example.kintai.dto.request.CorrectionCreateRequest;
import com.example.kintai.dto.response.CorrectionResponse;
import com.example.kintai.service.CorrectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** 修正申請エンドポイント */
@RestController
@RequestMapping("/attendance/corrections")
@RequiredArgsConstructor
public class CorrectionController {

    private final CorrectionService correctionService;

    @PostMapping
    public ResponseEntity<CorrectionResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CorrectionCreateRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(correctionService.createCorrection(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<CorrectionResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "applicant") String role,
            @RequestParam(required = false) String status) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(correctionService.listCorrections(userId, role, status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<CorrectionResponse> approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovalActionRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        var body = request != null ? request : new ApprovalActionRequest(null);
        return ResponseEntity.ok(correctionService.approve(id, userId, body));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<CorrectionResponse> reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovalActionRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        var body = request != null ? request : new ApprovalActionRequest(null);
        return ResponseEntity.ok(correctionService.reject(id, userId, body));
    }
}
