package ro.axonsoft.eval.minibank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dtos.PagedResponse;
import ro.axonsoft.eval.minibank.exceptions.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.service.TransactionService;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/api/accounts/{accountId}/transactions")
    public ResponseEntity<PagedResponse<Transaction>> getTransactions  (
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws ResourceNotFoundException {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionService.getAccountTransactions(accountId, pageable);

        return ResponseEntity.ok(new PagedResponse<>(transactionPage));
    }
}
