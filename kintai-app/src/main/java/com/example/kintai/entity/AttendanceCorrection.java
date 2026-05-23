package com.example.kintai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 勤怠修正申請 */
@Entity
@Table(name = "attendance_corrections")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttendanceCorrection {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private AttendanceRecord record;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "pending";

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "reviewer_comment")
    private String reviewerComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public CorrectionStatus getCorrectionStatus() {
        return CorrectionStatus.valueOf(status.toUpperCase());
    }

    public void setCorrectionStatus(CorrectionStatus s) {
        this.status = s.name().toLowerCase();
    }
}
