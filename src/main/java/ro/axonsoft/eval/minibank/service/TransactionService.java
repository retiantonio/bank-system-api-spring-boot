package ro.axonsoft.eval.minibank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.axonsoft.eval.minibank.exceptions.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.utility.TransactionType;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createTransactionPair(Transfer transfer, Account source, Account target) {
        Long bankId = 1L;

        BigDecimal amountForTarget = source.getCurrency().equals(target.getCurrency())
                ? transfer.getAmount()
                : transfer.getConvertedAmount();

        if (source.getId().equals(bankId)) {
            createEntry(target, transfer, TransactionType.DEPOSIT,
                    amountForTarget, null);
        } else if (target.getId().equals(bankId)) {
            createEntry(source, transfer, TransactionType.WITHDRAWAL,
                    transfer.getAmount().negate(), null);
        } else {
            createEntry(source, transfer, TransactionType.TRANSFER_OUT,
                    transfer.getAmount().negate(), target.getIban());

            createEntry(target, transfer, TransactionType.TRANSFER_IN,
                    amountForTarget, source.getIban());
        }
    }

    private void createEntry(Account account, Transfer transfer, TransactionType type,
                             BigDecimal amount, String counterpartyIban) {
        Transaction tx = new Transaction(
                account,
                type,
                amount,
                account.getCurrency(),
                account.getBalance(),
                counterpartyIban,
                transfer.getId()
        );

        transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getAccountTransactions(Long accountId, Pageable pageable)
            throws ResourceNotFoundException {

        accountService.getAccount(accountId);
        return transactionRepository.findByAccountIdOrderByTimeStampDesc(accountId, pageable);
    }
}