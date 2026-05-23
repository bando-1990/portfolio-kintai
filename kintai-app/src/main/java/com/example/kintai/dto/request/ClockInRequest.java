package com.example.kintai.dto.request;

import java.time.LocalDate;

public record ClockInRequest(
    LocalDate workDate,
    Double lat,
    Double lon
) {}
