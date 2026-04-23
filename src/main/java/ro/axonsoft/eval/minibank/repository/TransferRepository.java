package ro.axonsoft.eval.minibank.repository;

import org.hibernate.boot.models.annotations.internal.EmbeddableInstantiatorRegistrationAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ro.axonsoft.eval.minibank.model.Transfer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    List<Transfer> findBySourceIbanAndCreatedAtBetween(
            String sourceIban,
            Instant start,
            Instant end
    );
}

