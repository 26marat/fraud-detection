package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudDetectionServiceImpl Tests")
class FraudDetectionServiceImplTest {

    private final static Long ID = Long.valueOf(1);
    private final static String ACCOUNT_ID = "12345";
    private final static BigDecimal LOW_AMOUNT = new BigDecimal("500.00");
    private final static BigDecimal HIGH_AMOUNT = new BigDecimal("15000.00");
    private final static BigDecimal VERY_HIGH_AMOUNT = new BigDecimal("60000.00");
    private final static LocalDateTime NORMAL_HOUR =
            LocalDateTime.of(2024, 1, 15, 14, 30);
    private final static String LOCATION = "New York";

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FraudDetectionServiceImpl fraudDetectionService;

    @BeforeEach
    void setUp() {
        reset(transactionRepository);
    }

    @Test
    void testNormalTransaction() {
        // Given: Normal transaction - low amount, normal hours, normal frequency, single location
        Transaction transaction = createTransaction(
            LOW_AMOUNT,
            NORMAL_HOUR,
            LOCATION
        );

        when(transactionRepository.countByAccountIdAndTimestampBetween(anyString(), any(), any()))
            .thenReturn(10L);
        when(transactionRepository.findDistinctLocationsByAccountIdAndTimestampAfter(anyString(), any()))
            .thenReturn(Collections.singletonList("New York"));

        // When
        double fraudScore = fraudDetectionService.getFraudScore(transaction);
        boolean isFraudulent = fraudDetectionService.isFraudulent(transaction);

        // Then
        assertEquals(0.0, fraudScore);
        assertFalse(isFraudulent);
    }

    @Test
    void testSuspiciousButNotFraudulentTransaction() {
        // Given: High amount (0.3) + off-hours (0.2) = 0.5 (below 0.7 threshold)
        Transaction transaction = createTransaction(
            HIGH_AMOUNT,
            LocalDateTime.of(2024, 1, 15, 3, 30),
            LOCATION
        );

        when(transactionRepository.countByAccountIdAndTimestampBetween(anyString(), any(), any()))
            .thenReturn(10L);
        when(transactionRepository.findDistinctLocationsByAccountIdAndTimestampAfter(anyString(), any()))
            .thenReturn(Collections.singletonList("New York"));

        // When
        double fraudScore = fraudDetectionService.getFraudScore(transaction);
        boolean isFraudulent = fraudDetectionService.isFraudulent(transaction);

        // Then
        assertEquals(0.5, fraudScore);
        assertFalse(isFraudulent);
    }

    @Test
    void testFraudulentTransaction() {
        // Given: Very high amount (0.5) + high frequency (0.4) = 0.9 (above 0.7 threshold)
        Transaction transaction = createTransaction(
            VERY_HIGH_AMOUNT,
            LocalDateTime.of(2024, 1, 15, 14, 30),
            LOCATION
        );

        when(transactionRepository.countByAccountIdAndTimestampBetween(anyString(), any(), any()))
            .thenReturn(30L);
        when(transactionRepository.findDistinctLocationsByAccountIdAndTimestampAfter(anyString(), any()))
            .thenReturn(Collections.singletonList("New York"));

        // When
        double fraudScore = fraudDetectionService.getFraudScore(transaction);
        boolean isFraudulent = fraudDetectionService.isFraudulent(transaction);

        // Then
        assertEquals(0.9, fraudScore);
        assertTrue(isFraudulent);
    }

    private Transaction createTransaction(BigDecimal amount, LocalDateTime timestamp, String location) {
        Transaction transaction = new Transaction();
        transaction.setId(ID);
        transaction.setAccountId(ACCOUNT_ID);
        transaction.setAmount(amount);
        transaction.setTimestamp(timestamp);
        transaction.setLocation(location);
        return transaction;
    }
}
