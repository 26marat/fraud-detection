package com.maratzhukau.frauddetection.repository;

import com.maratzhukau.frauddetection.entity.Transaction;

public interface TransactionRepository {

    Transaction createTransaction(Transaction transaction);
}
