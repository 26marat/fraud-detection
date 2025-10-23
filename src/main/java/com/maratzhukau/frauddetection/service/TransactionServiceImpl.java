package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.domain.TransactionResponse;
import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.kafka.FraudAlertEvent;
import com.maratzhukau.frauddetection.kafka.FraudAlertProducer;
import com.maratzhukau.frauddetection.repository.TransactionRepository;
import com.maratzhukau.frauddetection.service.mapper.FraudAlertMapper;
import com.maratzhukau.frauddetection.service.mapper.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final FraudAlertMapper fraudAlertMapper;
    private final FraudDetectionService fraudDetectionService;
    private final FraudAlertProducer producer;

    public TransactionServiceImpl(TransactionRepository transactionRepository, 
                                TransactionMapper transactionMapper,
                                FraudAlertMapper fraudAlertMapper,
                                FraudDetectionService fraudDetectionService,
                                FraudAlertProducer producer) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.fraudAlertMapper = fraudAlertMapper;
        this.fraudDetectionService = fraudDetectionService;
        this.producer = producer;
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

            Transaction result = transactionRepository.save(transaction);
            if (isFraudulent) {
                FraudAlertEvent fraudAlertEvent = fraudAlertMapper.toFraudAlertEvent(transaction, fraudScore);
                producer.sendFraudAlert(fraudAlertEvent);
            }

            return transactionMapper.toResponse(result);

        } catch (Exception e) {
            logger.error("Error during fraud detection: ", e);
            Transaction result = transactionRepository.save(transaction);
            return transactionMapper.toResponse(result);
        }

    }
}
