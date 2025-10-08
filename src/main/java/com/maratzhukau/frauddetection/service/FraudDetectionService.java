package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.entity.Transaction;

public interface FraudDetectionService {

    boolean isFraudulent(Transaction transaction);
    
    /**
     * Gets the fraud score for a transaction (0.0 to 1.0)
     * @param transaction the transaction to analyze
     * @return fraud score between 0.0 (not fraudulent) and 1.0 (highly fraudulent)
     */
    double getFraudScore(Transaction transaction);
}
