package com.example.kintai.dto.response;

import com.example.kintai.entity.AttendanceCorrection;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CorrectionResponse(
    UUID id,
    UUID recordId,
    UUID applicantId,
    UUID approverId,
    String status,
    String reason,
    String reviewerComment,
    OffsetDateTime createdAt,
    OffsetDateTime decidedAt
) {
    public static CorrectionResponse from(AttendanceCorrection c) {
        return new CorrectionResponse(
            c.getId(),
            c.getRecord().getId(),
            c.getApplicant().getId(),
            c.getApprover() != null ? c.getApprover().getId() : null,
            c.getStatus(),
            c.getReason(),
            c.getReviewerComment(),
            c.getCreatedAt(),
            c.getDecidedAt()
        );
    }
}
