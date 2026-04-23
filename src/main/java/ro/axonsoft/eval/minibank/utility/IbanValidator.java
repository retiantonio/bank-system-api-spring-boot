package ro.axonsoft.eval.minibank.utility;

import org.apache.commons.validator.routines.IBANValidator;
import ro.axonsoft.eval.minibank.exceptions.BusinessRuleException;
import ro.axonsoft.eval.minibank.exceptions.InvalidTransactionException;

import java.util.Set;

public class IbanValidator {

    private static final IBANValidator APACHE_VALIDATOR = IBANValidator.getInstance();

    private static final Set<String> SEPA_COUNTRIES = Set.of(
            "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GR", "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PO", "PT", "RO", "SE", "SI", "SK",
            "IS", "LI", "NO", "CH", "AD", "MC", "SM", "VA", "GB"
    );

    public static void validate(String iban) throws InvalidTransactionException {
        if (!APACHE_VALIDATOR.isValid(iban)) {
            throw new InvalidTransactionException("The provided IBAN is mathematically invalid or malformed.");
        }
    }

    public static void checkSepa(String sourceIban, String targetIban) {
        if (!isSepa(sourceIban) || !isSepa(targetIban)) {
            throw new BusinessRuleException("One or both accounts are outside the SEPA zone.");
        }
    }

    private static boolean isSepa(String iban) {
        if (iban == null || iban.length() < 2) return false;
        String countryCode = iban.substring(0, 2).toUpperCase();
        return SEPA_COUNTRIES.contains(countryCode);
    }

}
