package com.mvp.facilitapay.infra.repo;

import com.mvp.facilitapay.domain.model.PaymentSplit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentSplitRepository extends JpaRepository<PaymentSplit, UUID> {

    List<PaymentSplit> findByPaymentId(UUID paymentId);

    List<PaymentSplit> findByPayment_UserId(UUID userId);
}

