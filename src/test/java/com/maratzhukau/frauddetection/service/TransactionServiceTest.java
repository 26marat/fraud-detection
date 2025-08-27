package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.repository.TransactionRepository;
import com.maratzhukau.frauddetection.service.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    private final static Long ID = Long.valueOf(1);
    private final static String ACCOUNT_ID = "12345";
    private final static BigDecimal AMOUNT = new BigDecimal(100.0);
    private final static LocalDateTime TIMESTAMP = LocalDateTime.now();
    private final static String LOCATION = "New York";

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    public void testProcessTransactionCallsMapperAndRepository() {

        TransactionRequest transactionRequest = new TransactionRequest(ACCOUNT_ID,
                AMOUNT, TIMESTAMP, LOCATION);
        Transaction transactionWithoutId = buildTransactionWithoutId();
        Transaction expectedTransaction = new Transaction(ID, ACCOUNT_ID, AMOUNT, TIMESTAMP, LOCATION);

        when(transactionMapper.toEntity(transactionRequest)).thenReturn(transactionWithoutId);
        when(transactionRepository.save(transactionWithoutId)).thenReturn(expectedTransaction);

        transactionService.processTransaction(transactionRequest);

        verify(transactionMapper, times(1)).toEntity(transactionRequest);
        verify(transactionRepository, times(1)).save(transactionWithoutId);
    }

    private Transaction buildTransactionWithoutId() {
        Transaction transaction = new Transaction();
        transaction.setAccountId(ACCOUNT_ID);
        transaction.setAmount(AMOUNT);
        transaction.setTimestamp(TIMESTAMP);
        transaction.setLocation(LOCATION);
        transaction.setIsFraudulent(false);
        return transaction;
    }
}
