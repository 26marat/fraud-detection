package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.domain.TransactionResponse;
import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.repository.TransactionRepository;
import com.maratzhukau.frauddetection.service.mapper.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final FraudDetectionService fraudDetectionService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, 
                                TransactionMapper transactionMapper,
                                FraudDetectionService fraudDetectionService) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Override
    public TransactionResponse processTransaction(TransactionRequest transactionRequest) {
        
        Transaction transaction = transactionMapper.toEntity(transactionRequest);
        
        try {
            boolean isFraudulent = fraudDetectionService.isFraudulent(transaction);
            double fraudScore = fraudDetectionService.getFraudScore(transaction);
            transaction.setIsFraudulent(isFraudulent);

            logger.info("Transaction fraud analysis completed: accountId={}, amount={}, fraudScore={}, isFraudulent={}",
                    transaction.getAccountId(), transaction.getAmount(), fraudScore, isFraudulent);
        } catch(Exception e) {
            logger.error("Error during fraud detection: ", e);
        }

        Transaction result = transactionRepository.save(transaction);

        return transactionMapper.toResponse(result);
    }
}
