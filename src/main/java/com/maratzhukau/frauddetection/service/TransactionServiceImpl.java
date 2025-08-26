package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.domain.TransactionResponse;
import com.maratzhukau.frauddetection.entity.Transaction;
import com.maratzhukau.frauddetection.repository.TransactionRepository;
import com.maratzhukau.frauddetection.service.mapper.TransactionMapper;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public TransactionResponse processTransaction(TransactionRequest transactionRequest) {

        Transaction transaction = transactionMapper.toEntity(transactionRequest);
        Transaction result = transactionRepository.createTransaction(transaction);

        return transactionMapper.toResponse(result);
    }
}
