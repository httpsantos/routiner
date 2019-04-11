package com.rodrigoom.routiner.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import com.rodrigoom.routiner.Utils;

public class ExecutionDTO {
    String executedAt;

    public String getExecutedAt() {
        return executedAt;
    }

    ExecutionDTO(Long executionTimestamp) {
        LocalDateTime executionDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(executionTimestamp),
                TimeZone.getDefault().toZoneId()
        );

        this.executedAt = Utils.formatDate(executionDate);
    }
}
