package com.maratzhukau.frauddetection.service;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.domain.TransactionResponse;

public interface TransactionService {

    TransactionResponse processTransaction(TransactionRequest transactionRequest);
}
