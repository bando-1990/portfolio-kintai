package com.example.kintai.dto.response;

import java.time.LocalDate;

public record DailySummaryResponse(
    LocalDate workDate,
    String status,
    Integer grossWorkMinutes,
    Integer autoBreakMinutes,
    Integer netWorkMinutes
) {}
