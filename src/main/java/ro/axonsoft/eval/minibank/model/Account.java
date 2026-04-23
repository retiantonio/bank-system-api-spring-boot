package ro.axonsoft.eval.minibank.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ro.axonsoft.eval.minibank.exceptions.BusinessRuleException;
import ro.axonsoft.eval.minibank.exceptions.InvalidTransactionException;
import ro.axonsoft.eval.minibank.utility.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "accountType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SavingsAccount.class, name = "SAVINGS"),
        @JsonSubTypes.Type(value = CheckingAccount.class, name = "CHECKING")
})

@JsonPropertyOrder({
        "id",
        "ownerName",
        "iban",
        "currency",
        "accountType",
        "balance",
        "createdAt"
})
@org.hibernate.annotations.Check(constraints = "balance >= 0")
public abstract class Account {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, updatable = false)
    private String ownerName;

    @NotBlank(message = "IBAN is required")
    @Column(nullable = false, updatable = false, unique = true)
    private String iban;

    //@NotBlank(message = "Currency is required")
    @NotNull(message = "Currency is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Currency currency;

    @CreatedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = new BigDecimal("0.00");

    public Account() {}

    public Account(String ownerName, String iban, Currency currency) {
        this.ownerName = ownerName;
        this.iban = iban;
        this.currency = currency;
    }

    public Account(String ownerName, String iban, Currency currency, BigDecimal balance) {
        this.ownerName = java.util.Objects.requireNonNull(ownerName, "Owner name is required");
        this.currency = java.util.Objects.requireNonNull(currency, "Currency is required");

        ro.axonsoft.eval.minibank.utility.IbanValidator.validate(iban);
        this.iban = iban;

        if (balance == null) {
            throw new InvalidTransactionException("Initial balance is required");
        }

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("An account cannot be created with a negative balance.");
        }

        this.balance = balance;
    }

    @NotBlank(message = "Account type cannot be blank")
    @JsonProperty("accountType")
    public String getAccountType() {
        String type = org.hibernate.Hibernate.getClass(this)
                .getSimpleName()
                .replace("Account", "")
                .toUpperCase();

        if (type.isEmpty()) {
            throw new InvalidTransactionException("Could not determine account type");
        }

        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getIban() {
        return iban;
    }

    public void setId(Long id) {
        this.id = id;
    }


    ////////////////////////////////
    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
