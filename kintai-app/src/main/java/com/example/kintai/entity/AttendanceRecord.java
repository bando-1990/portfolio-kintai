package com.example.kintai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** 勤怠レコード */
@Entity
@Table(
    name = "attendance_records",
    uniqueConstraints = @UniqueConstraint(name = "uq_user_date", columnNames = {"user_id", "work_date"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "clock_in_at")
    private OffsetDateTime clockInAt;

    @Column(name = "clock_out_at")
    private OffsetDateTime clockOutAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.NOT_STARTED;

    @Column(name = "source", nullable = false, length = 20)
    @Builder.Default
    private String source = "WEB";

    @Column(name = "notes")
    private String notes;

    @Column(name = "gross_work_minutes")
    private Integer grossWorkMinutes;

    @Column(name = "auto_break_minutes")
    @Builder.Default
    private Integer autoBreakMinutes = 0;

    @Column(name = "net_work_minutes")
    private Integer netWorkMinutes;

    @Column(name = "calc_policy_code", length = 50)
    private String calcPolicyCode;

    @Column(name = "calc_at")
    private OffsetDateTime calcAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calc_trace")
    private String calcTrace;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
