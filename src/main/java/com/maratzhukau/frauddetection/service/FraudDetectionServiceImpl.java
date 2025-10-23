package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionServiceImpl.class);
    
    private final TransactionRepository transactionRepository;

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000.00");
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final int MAX_TRANSACTIONS_PER_DAY = 50;
    private static final int MAX_LOCATIONS_PER_DAY = 3;
    private static final double FRAUD_SCORE_THRESHOLD = 0.7;
    
    public FraudDetectionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    public boolean isFraudulent(Transaction transaction) {
        double fraudScore = getFraudScore(transaction);
        boolean isFraud = fraudScore >= FRAUD_SCORE_THRESHOLD;
        
        logger.info("Transaction analysis for Account: {} fraud analysis: score={}, isFraudulent={}",
                   transaction.getAccountId(), fraudScore, isFraud);
        
        return isFraud;
    }
    
    @Override
    public double getFraudScore(Transaction transaction) {
        double score = 0.0;

        score += checkHighAmountRule(transaction);
        score += checkFrequencyRule(transaction);
        score += checkLocationAnomalyRule(transaction);
        score += checkOffHoursRule(transaction);

        return Math.min(score, 1.0);
    }
    
    /**
     * Rule 1: Check for unusually high transaction amounts
     */
    private double checkHighAmountRule(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        
        if (amount.compareTo(VERY_HIGH_AMOUNT_THRESHOLD) >= 0) {
            logger.debug("Very high amount detected: {}", amount);
            return 0.5; // High risk
        } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            logger.debug("High amount detected: {}", amount);
            return 0.3; // Medium-high risk
        }
        
        return 0.0;
    }
    
    /**
     * Rule 2: Check transaction frequency patterns
     */
    private double checkFrequencyRule(Transaction transaction) {
        LocalDateTime now = transaction.getTimestamp();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneDayAgo = now.minusDays(1);

        long transactionsLastHour = transactionRepository.countByAccountIdAndTimestampBetween(
            transaction.getAccountId(), oneHourAgo, now);

        long transactionsLastDay = transactionRepository.countByAccountIdAndTimestampBetween(
            transaction.getAccountId(), oneDayAgo, now);
        
        double score = 0.0;
        
        if (transactionsLastHour > MAX_TRANSACTIONS_PER_HOUR) {
            logger.debug("High frequency detected: {} transactions in last hour", transactionsLastHour);
            score += 0.4;
        }
        
        if (transactionsLastDay > MAX_TRANSACTIONS_PER_DAY) {
            logger.debug("Very high daily frequency detected: {} transactions in last day", transactionsLastDay);
            score += 0.3;
        }
        
        return score;
    }
    
    /**
     * Rule 3: Check for location anomalies
     */
    private double checkLocationAnomalyRule(Transaction transaction) {
        if (transaction.getLocation() == null) {
            return 0.0;
        }
        
        LocalDateTime oneDayAgo = transaction.getTimestamp().minusDays(1);
        
        List<String> recentLocations = transactionRepository
            .findDistinctLocationsByAccountIdAndTimestampAfter(
                transaction.getAccountId(), oneDayAgo);
        
        if (recentLocations.size() > MAX_LOCATIONS_PER_DAY) {
            logger.debug("Multiple locations detected: {} locations in last day", recentLocations.size());
            return 0.3;
        }
        
        return 0.0;
    }
    
    /**
     * Rule 4: Check for off-hours transactions (potential indicator of fraud)
     */
    private double checkOffHoursRule(Transaction transaction) {
        int hour = transaction.getTimestamp().getHour();

        if (hour >= 2 && hour <= 6) {
            logger.debug("Off-hours transaction detected at hour: {}", hour);
            return 0.2;
        }
        
        return 0.0;
    }

}
