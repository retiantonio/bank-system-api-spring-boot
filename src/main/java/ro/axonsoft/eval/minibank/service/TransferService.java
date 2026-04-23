package ro.axonsoft.eval.minibank.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.axonsoft.eval.minibank.exceptions.BusinessRuleException;
import ro.axonsoft.eval.minibank.exceptions.InvalidTransactionException;
import ro.axonsoft.eval.minibank.exceptions.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.SavingsAccount;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;
import ro.axonsoft.eval.minibank.utility.ExchangeConfiguration;
import ro.axonsoft.eval.minibank.utility.IbanValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
public class TransferService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private ExchangeConfiguration exchangeConfiguration;

    public Transfer getTransfer(Long id) throws ResourceNotFoundException {
        return transferRepository.findById(id)
                .orElseThrow(() ->
                new ResourceNotFoundException(
                "Transfer with ID " + id + " not found"));
    }

    public Page<Transfer> getFilteredTransfers(String iban, Instant from, Instant to, Pageable pageable) {
        Specification<Transfer> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (iban != null && !iban.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.equal(root.get("sourceIban"), iban),
                            cb.equal(root.get("targetIban"), iban)
                    )
            );
        }

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        return transferRepository.findAll(spec, pageable);
    }

    @Transactional
    public Transfer executeTransfer(Transfer transfer)
            throws ResourceNotFoundException,
                   InvalidTransactionException,
                   BusinessRuleException {

        if (transfer.getIdempotencyKey() != null) {
            Optional<Transfer> existing = transferRepository.findByIdempotencyKey(transfer.getIdempotencyKey());
            if (existing.isPresent()) return existing.get();
        }

        String sourceIban = transfer.getSourceIban();
        String targetIban = transfer.getTargetIban();
        if(sourceIban.equals(targetIban))
            throw new InvalidTransactionException("Source and target IBANs must be different");

        IbanValidator.checkSepa(sourceIban, targetIban);

        String firstIban = sourceIban.compareTo(targetIban) < 0 ? sourceIban : targetIban;
        String secondIban = firstIban.equals(sourceIban) ? targetIban : sourceIban;

        Account firstAccount = accountService.getAccountByIbanWithLock(firstIban);
        Account secondAccount = accountService.getAccountByIbanWithLock(secondIban);

        Account source = firstIban.equals(sourceIban) ? firstAccount : secondAccount;
        Account target = firstIban.equals(targetIban) ? firstAccount : secondAccount;

        if ("SAVINGS".equals(source.getAccountType())) {
            validateSavingsLimit(source, transfer.getAmount());
        }

        transfer.setCurrency(source.getCurrency());
        transfer.setTargetCurrency(target.getCurrency());

        if (source.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }

        BigDecimal amountToCredit = calculateTargetAmount(transfer, source, target);

        source.setBalance(source.getBalance().subtract(transfer.getAmount()));
        target.setBalance(target.getBalance().add(amountToCredit));

        accountService.saveAccount(source);
        accountService.saveAccount(target);

        Transfer savedTransfer = transferRepository.save(transfer);
        transactionService.createTransactionPair(savedTransfer, source, target);

        return savedTransfer;
    }

    private BigDecimal calculateTargetAmount(Transfer transfer, Account source, Account target) {
        if (source.getCurrency().equals(target.getCurrency())) {
            transfer.setExchangeRate(null);
            transfer.setConvertedAmount(null);
            return transfer.getAmount();
        }

        BigDecimal sourceToRon = exchangeConfiguration.getRates().get(source.getCurrency().name());
        BigDecimal targetToRon = exchangeConfiguration.getRates().get(target.getCurrency().name());

        BigDecimal effectiveRate = sourceToRon.divide(targetToRon, 6, RoundingMode.HALF_EVEN);
        transfer.setExchangeRate(effectiveRate);

        BigDecimal amountToCredit = transfer.getAmount().multiply(effectiveRate).setScale(2, RoundingMode.HALF_EVEN);
        transfer.setConvertedAmount(amountToCredit);

        return amountToCredit;
    }

    private void validateSavingsLimit(Account source, BigDecimal newAmount) throws BusinessRuleException {

        Instant startOfToday = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant endOfToday = LocalDate.now()
                .atTime(LocalTime.MAX)
                .atZone(ZoneOffset.UTC)
                .toInstant();

        List<Transfer> todaysTransfers = transferRepository
                .findBySourceIbanAndCreatedAtBetween(source.getIban(), startOfToday, endOfToday);

        BigDecimal eurRate = exchangeConfiguration.getRates().get("EUR");
        BigDecimal spentInEur = BigDecimal.ZERO;

        for (Transfer t : todaysTransfers) {
            BigDecimal amountInRon = t.getAmount().multiply(exchangeConfiguration.getRates().get(t.getCurrency().name()));
            BigDecimal amountInEur = amountInRon.divide(eurRate, 2, RoundingMode.HALF_EVEN);
            spentInEur = spentInEur.add(amountInEur);
        }

        BigDecimal currentInRon = newAmount.multiply(exchangeConfiguration.getRates().get(source.getCurrency().name()));
        BigDecimal currentInEur = currentInRon.divide(eurRate, 2, RoundingMode.HALF_EVEN);

        BigDecimal totalAfterTransfer = spentInEur.add(currentInEur);
        BigDecimal limit = new BigDecimal("5000.00");

        if (totalAfterTransfer.compareTo(limit) > 0) {
            throw new BusinessRuleException("Daily limit of 5000.00 EUR exceeded");
        }
    }
}
