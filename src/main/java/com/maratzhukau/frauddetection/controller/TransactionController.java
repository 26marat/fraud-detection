package com.maratzhukau.frauddetection.controller;

import com.maratzhukau.frauddetection.domain.TransactionRequest;
import com.maratzhukau.frauddetection.domain.TransactionResponse;
import com.maratzhukau.frauddetection.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest) {
        TransactionResponse response = transactionService.processTransaction(transactionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
