package com.example.kintai.service;

import com.example.kintai.dto.request.ApprovalActionRequest;
import com.example.kintai.dto.request.CorrectionCreateRequest;
import com.example.kintai.dto.response.CorrectionResponse;
import com.example.kintai.entity.AttendanceCorrection;
import com.example.kintai.repository.AttendanceCorrectionRepository;
import com.example.kintai.repository.AttendanceRecordRepository;
import com.example.kintai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** 修正申請サービス */
@Service
@RequiredArgsConstructor
public class CorrectionService {

    private final AttendanceCorrectionRepository correctionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional
    public CorrectionResponse createCorrection(UUID applicantId, CorrectionCreateRequest request) {
        var record = recordRepository.findById(request.recordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "勤怠レコードが見つかりません"));
        var applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        var correction = AttendanceCorrection.builder()
                .id(UUID.randomUUID())
                .record(record)
                .applicant(applicant)
                .status("pending")
                .reason(request.reason())
                .createdAt(OffsetDateTime.now())
                .build();

        return CorrectionResponse.from(correctionRepository.save(correction));
    }

    @Transactional(readOnly = true)
    public List<CorrectionResponse> listCorrections(UUID userId, String role, String status) {
        List<AttendanceCorrection> list;
        if ("applicant".equals(role)) {
            list = status != null
                    ? correctionRepository.findByApplicantIdAndStatus(userId, status)
                    : correctionRepository.findByApplicantIdOrderByCreatedAtDesc(userId);
        } else {
            list = status != null
                    ? correctionRepository.findByApproverIdAndStatus(userId, status)
                    : correctionRepository.findByApproverIdOrderByCreatedAtDesc(userId);
        }
        return list.stream().map(CorrectionResponse::from).toList();
    }

    @Transactional
    public CorrectionResponse approve(UUID correctionId, UUID approverId, ApprovalActionRequest request) {
        return updateStatus(correctionId, approverId, "approved", request.comment());
    }

    @Transactional
    public CorrectionResponse reject(UUID correctionId, UUID approverId, ApprovalActionRequest request) {
        return updateStatus(correctionId, approverId, "rejected", request.comment());
    }

    private CorrectionResponse updateStatus(UUID id, UUID approverId, String newStatus, String comment) {
        var correction = correctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申請が見つかりません"));

        if (!"pending".equals(correction.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "既に処理済みの申請です");
        }

        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "承認者が見つかりません"));

        correction.setStatus(newStatus);
        correction.setApprover(approver);
        correction.setReviewerComment(comment);
        correction.setDecidedAt(OffsetDateTime.now());

        return CorrectionResponse.from(correctionRepository.save(correction));
    }
}
