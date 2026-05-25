package com.example.kintai.service;

import com.example.kintai.dto.request.ApprovalActionRequest;
import com.example.kintai.dto.request.CorrectionCreateRequest;
import com.example.kintai.dto.response.CorrectionResponse;
import com.example.kintai.entity.AttendanceCorrection;
import com.example.kintai.entity.AttendanceStatus;
import com.example.kintai.repository.AttendanceCorrectionRepository;
import com.example.kintai.repository.AttendanceRecordRepository;
import com.example.kintai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;


/** 修正申請サービス */
@Slf4j
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
                .requestedClockIn(request.requestedClockIn())
                .requestedClockOut(request.requestedClockOut())
                .createdAt(OffsetDateTime.now())
                .build();

        log.info("修正申請作成: applicantId={}, recordId={}", applicantId, request.recordId());
        return CorrectionResponse.from(correctionRepository.save(correction));
    }

    @Transactional(readOnly = true)
    public Page<CorrectionResponse> listCorrections(UUID userId, String role, String status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        Page<AttendanceCorrection> result;
        if ("applicant".equals(role)) {
            result = status != null
                    ? correctionRepository.findByApplicantIdAndStatus(userId, status, pageable)
                    : correctionRepository.findByApplicantIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            // 承認者ロールは全申請を返す（pending申請は approver 未設定のため全件クエリ）
            result = status != null
                    ? correctionRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    : correctionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return result.map(CorrectionResponse::from);
    }

    @Transactional
    public CorrectionResponse approve(UUID correctionId, UUID approverId, ApprovalActionRequest request) {
        var correction = findPending(correctionId);
        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "承認者が見つかりません"));

        correction.setStatus("approved");
        correction.setApprover(approver);
        correction.setReviewerComment(request.comment());
        correction.setDecidedAt(OffsetDateTime.now());

        // 希望時刻が指定されていれば勤怠レコードへ反映・再計算
        var record = correction.getRecord();
        if (correction.getRequestedClockIn() != null) {
            record.setClockInAt(correction.getRequestedClockIn());
        }
        if (correction.getRequestedClockOut() != null) {
            record.setClockOutAt(correction.getRequestedClockOut());
            record.setStatus(AttendanceStatus.COMPLETED);
        }
        if (record.getClockInAt() != null && record.getClockOutAt() != null) {
            long grossMinutes = Duration.between(record.getClockInAt(), record.getClockOutAt()).toMinutes();
            int autoBreak = grossMinutes >= 480 ? 60 : grossMinutes >= 360 ? 45 : 0;
            record.setGrossWorkMinutes((int) grossMinutes);
            record.setAutoBreakMinutes(autoBreak);
            record.setNetWorkMinutes((int) grossMinutes - autoBreak);
            record.setCalcAt(OffsetDateTime.now());
        }
        record.setUpdatedAt(OffsetDateTime.now());
        recordRepository.save(record);

        log.info("修正申請承認: correctionId={}, approverId={}", correctionId, approverId);
        return CorrectionResponse.from(correctionRepository.save(correction));
    }

    @Transactional
    public CorrectionResponse reject(UUID correctionId, UUID approverId, ApprovalActionRequest request) {
        var correction = findPending(correctionId);
        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "承認者が見つかりません"));

        correction.setStatus("rejected");
        correction.setApprover(approver);
        correction.setReviewerComment(request.comment());
        correction.setDecidedAt(OffsetDateTime.now());

        log.info("修正申請却下: correctionId={}, approverId={}", correctionId, approverId);
        return CorrectionResponse.from(correctionRepository.save(correction));
    }

    private AttendanceCorrection findPending(UUID id) {
        var correction = correctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申請が見つかりません"));
        if (!"pending".equals(correction.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "既に処理済みの申請です");
        }
        return correction;
    }
}
