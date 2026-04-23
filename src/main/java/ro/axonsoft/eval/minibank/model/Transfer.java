package ro.axonsoft.eval.minibank.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ro.axonsoft.eval.minibank.utility.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfers")
@EntityListeners(AuditingEntityListener.class)

@JsonPropertyOrder({
        "id",
        "sourceIban",
        "targetIban",
        "amount",
        "currency",
        "targetCurrency",
        "exchangeRate",
        "convertedAmount",
        "idempotencyKey",
        "createdAt"
})
public class Transfer {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Source's IBAN is required")
    @NotBlank(message = "Source's IBAN is required")
    @Column(nullable = false, updatable = false)
    private String sourceIban;

    @NotNull(message = "Target's IBAN is required")
    @NotBlank(message = "Target's IBAN is required")
    @Column(nullable = false, updatable = false)
    private String targetIban;

    @NotNull(message = "Amount is required")
    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    @Positive(message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency = null;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, updatable = false)
    private Currency targetCurrency = null;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate = null;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false, precision = 19, scale = 2)
    private BigDecimal convertedAmount = null;

    @Column(unique = true, updatable = false)
    private String idempotencyKey;

    @CreatedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    public Transfer() {}

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTargetIban() {
        return targetIban;
    }

    public String getSourceIban() {
        return sourceIban;
    }
    ///////////////////////////
    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceIban(String sourceIban) {
        this.sourceIban = sourceIban;
    }

    public void setTargetIban(String targetIban) {
        this.targetIban = targetIban;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
