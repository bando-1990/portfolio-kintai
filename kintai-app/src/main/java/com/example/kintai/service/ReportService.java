package com.example.kintai.service;

import com.example.kintai.dto.response.DailySummaryResponse;
import com.example.kintai.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** 集計レポートサービス */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttendanceRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public List<DailySummaryResponse> getDailySummary(UUID userId, LocalDate from, LocalDate to) {
        return recordRepository
                .findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(userId, from, to)
                .stream()
                .map(r -> new DailySummaryResponse(
                        r.getWorkDate(),
                        r.getStatus().name(),
                        r.getGrossWorkMinutes(),
                        r.getAutoBreakMinutes(),
                        r.getNetWorkMinutes()
                ))
                .toList();
    }
}
