package com.example.kintai.dto.request;

import java.time.LocalDate;

public record ClockOutRequest(
    LocalDate workDate
) {}
