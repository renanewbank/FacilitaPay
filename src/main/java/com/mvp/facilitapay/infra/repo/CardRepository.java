package com.mvp.facilitapay.infra.repo;

import com.mvp.facilitapay.domain.model.Card;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByUserId(UUID userId);
}

