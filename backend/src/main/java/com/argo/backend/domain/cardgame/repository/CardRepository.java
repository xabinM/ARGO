package com.argo.backend.domain.cardgame.repository;

import com.argo.backend.domain.cardgame.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllBySpotId(Long id);

    List<Card> findAllBySpotIsNull();
}