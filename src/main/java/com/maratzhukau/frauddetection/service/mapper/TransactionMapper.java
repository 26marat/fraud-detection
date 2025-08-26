package com.maratzhukau.frauddetection.service.mapper;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.domain.TransactionResponse;
import com.maratzhukau.frauddetection.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(request.accountId());
        transaction.setAmount(request.amount());
        transaction.setTimestamp(request.timestamp());
        transaction.setLocation(request.location());
        transaction.setIsFraudulent(false);
        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getLocation(),
                transaction.getIsFraudulent()
        );
    }
}
