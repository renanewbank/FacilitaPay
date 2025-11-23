package com.mvp.facilitapay.infra.repo;

import com.mvp.facilitapay.domain.model.Payout;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    Optional<Payout> findByPaymentId(UUID paymentId);

    Optional<Payout> findByPayment_UserId(UUID userId);
}

