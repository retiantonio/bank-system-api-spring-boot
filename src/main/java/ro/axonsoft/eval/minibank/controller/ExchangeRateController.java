package ro.axonsoft.eval.minibank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axonsoft.eval.minibank.utility.ExchangeConfiguration;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    @Autowired
    private ExchangeConfiguration exchangeConfiguration;

    public ExchangeRateController(ExchangeConfiguration exchangeRateConfig) {
        this.exchangeConfiguration = exchangeRateConfig;
    }

    @GetMapping
    public ResponseEntity<Map<String, BigDecimal>> getExchangeRates() {
        return ResponseEntity.ok(exchangeConfiguration.getRates());
    }
}