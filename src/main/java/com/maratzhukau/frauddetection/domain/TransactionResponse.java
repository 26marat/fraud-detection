package com.maratzhukau.frauddetection.domain;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        @NotNull
        String accountId,

        @NotNull
        @Digits(integer = 19, fraction = 4)
        BigDecimal amount,

        @NotNull
        LocalDateTime timestamp,

        @NotBlank
        String location,

        @NotNull
        Boolean isFraudulent) {}
