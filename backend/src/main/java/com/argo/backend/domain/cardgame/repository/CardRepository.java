package com.argo.backend.domain.cardgame.repository;

import com.argo.backend.domain.cardgame.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}