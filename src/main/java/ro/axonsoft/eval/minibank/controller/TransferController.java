package ro.axonsoft.eval.minibank.controller;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ro.axonsoft.eval.minibank.dtos.PagedResponse;
import ro.axonsoft.eval.minibank.exceptions.InvalidTransactionException;
import ro.axonsoft.eval.minibank.exceptions.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.service.TransferService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransferById(@PathVariable Long id)
            throws ResourceNotFoundException {

        Transfer transfer = transferService.getTransfer(id);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Transfer>> getTransfers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transfer> transferPage = transferService.getFilteredTransfers(iban, fromDate, toDate, pageable);

        return ResponseEntity.ok(new PagedResponse<>(transferPage));
    }

    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@Valid @RequestBody Transfer transfer)
            throws ResourceNotFoundException, InvalidTransactionException {

        return new ResponseEntity<>(transferService.executeTransfer(transfer), HttpStatus.CREATED);
    }
}
