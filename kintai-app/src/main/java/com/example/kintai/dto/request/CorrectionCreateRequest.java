package com.example.kintai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CorrectionCreateRequest(
    @NotNull UUID recordId,
    @NotBlank String reason
) {}
