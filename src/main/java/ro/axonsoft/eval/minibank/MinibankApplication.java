package ro.axonsoft.eval.minibank;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;
import ro.axonsoft.eval.minibank.utility.Currency;
import ro.axonsoft.eval.minibank.utility.ExchangeConfiguration;
import ro.axonsoft.eval.minibank.model.CheckingAccount;
import ro.axonsoft.eval.minibank.repository.AccountRepository;

import java.math.BigDecimal;

@SpringBootApplication
@EnableJpaAuditing
//@EnableConfigurationProperties(ExchangeConfiguration.class)
public class MinibankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinibankApplication.class, args);
    }

    @Bean
    @Transactional
    public CommandLineRunner seedData(AccountRepository repository) {
        return args -> {
            CheckingAccount initialAccount = new CheckingAccount(
                    "System Bank Account",
                    "RO49AAAA1B31007593840000",
                    Currency.EUR,
                    new BigDecimal("999999999999999.99"));

            repository.save(initialAccount);
            repository.flush();
            System.out.println("Bank Account Initialized");
        };
    }
}
