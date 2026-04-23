package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.Entity;
import ro.axonsoft.eval.minibank.utility.Currency;

import java.math.BigDecimal;

@Entity
public class CheckingAccount extends Account {

    public CheckingAccount() {}

    public CheckingAccount(String ownerName, String iban, Currency currency) {
       super(ownerName, iban, currency);
    }

    public CheckingAccount(String ownerName, String iban, Currency currency, BigDecimal balance) {
        super(ownerName, iban, currency, balance);
    }
}
