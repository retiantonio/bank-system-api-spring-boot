package ro.axonsoft.eval.minibank.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ro.axonsoft.eval.minibank.exceptions.InvalidTransactionException;
import ro.axonsoft.eval.minibank.utility.Currency;
import ro.axonsoft.eval.minibank.utility.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)

@JsonPropertyOrder({
        "id",
        "timestamp",
        "type",
        "amount",
        "currency",
        "balanceAfter",
        "counterpartyIban",
        "transferId"
})
public class Transaction {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timeStamp;

    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TransactionType type;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String counterpartyIban;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false)
    private Long transferId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;

    public Transaction() {}

    public Transaction(Account account, TransactionType type, BigDecimal amount,
                       Currency currency, BigDecimal balanceAfter,
                       String counterpartyIban, Long transferId)
            throws InvalidTransactionException, NullPointerException {

        this.account = java.util.Objects.requireNonNull(account, "Account is required");
        this.type = java.util.Objects.requireNonNull(type, "Transaction type is required");
        this.currency = java.util.Objects.requireNonNull(currency, "Currency is required");
        this.transferId = java.util.Objects.requireNonNull(transferId, "Linked Transfer ID is required");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidTransactionException("Transaction amount cannot be null or zero");
        }

        if (isDebitType(type) && amount.compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidTransactionException("Debit transactions (WITHDRAWAL/TRANSFER_OUT) must have a negative amount");
        }

        if (isCreditType(type) && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Credit transactions (Deposit/Transfer_In) must have a positive amount");
        }

        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartyIban = counterpartyIban;
    }

    private boolean isDebitType(TransactionType type) {
        return type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER_OUT;
    }

    private boolean isCreditType(TransactionType type) {
        return type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN;
    }

    public Account getAccount() {
        return account;
    }
    /////////////////
    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getCounterpartyIban() {
        return counterpartyIban;
    }

    public void setCounterpartyIban(String counterpartyIban) {
        this.counterpartyIban = counterpartyIban;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
