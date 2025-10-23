package com.maratzhukau.frauddetection.service.mapper;

import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.kafka.FraudAlertEvent;
import org.springframework.stereotype.Component;

@Component
public class FraudAlertMapper {

    public FraudAlertEvent toFraudAlertEvent(Transaction transaction, double fraudScore) {
        return new FraudAlertEvent(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getLocation(),
                fraudScore
        );
    }
}
