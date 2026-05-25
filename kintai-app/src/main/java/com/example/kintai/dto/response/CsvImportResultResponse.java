package com.example.kintai.dto.response;

import java.util.List;

public record CsvImportResultResponse(
    int created,
    int skipped,
    int errors,
    List<String> errorDetails
) {}
