package com.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.dto.CreateTransactionRequest;
import com.project.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    public String transact(@RequestBody @Valid CreateTransactionRequest createTransactionRequest) throws JsonProcessingException {
        return transactionService.transact(createTransactionRequest);
    }
}
