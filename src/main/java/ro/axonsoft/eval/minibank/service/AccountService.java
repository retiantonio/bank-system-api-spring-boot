package ro.axonsoft.eval.minibank.service;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.axonsoft.eval.minibank.exceptions.BusinessRuleException;
import ro.axonsoft.eval.minibank.exceptions.InvalidTransactionException;
import ro.axonsoft.eval.minibank.exceptions.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.utility.IbanValidator;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.repository.AccountRepository;


@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account createAccount(Account account) throws InvalidTransactionException {
        account.setId(null);

        if (accountRepository.existsByIban(account.getIban())) {
            throw new BusinessRuleException("IBAN already in use");
        }

        IbanValidator.validate(account.getIban());
        return accountRepository.save(account);
    }

    public Account getAccount(Long id) throws ResourceNotFoundException {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                new ResourceNotFoundException(
                "Account with ID " + id + " not found"));
    }

    public Account getAccountByIban(String iban) throws ResourceNotFoundException {
        return accountRepository.findByIban(iban)
                .orElseThrow(() ->
                new ResourceNotFoundException(
                "Account with IBAN " + iban + " not found"));
    }

    @Transactional
    public Account getAccountByIbanWithLock(String iban) {
        return accountRepository.findByIbanWithLock(iban)
                .orElseThrow(() ->
                new ResourceNotFoundException(
                "Account with IBAN " + iban + " not found"));
    }

    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }
}
