package com.argo.backend.domain.cardgame.repository;

import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardGameMatchRepository extends JpaRepository<CardGameMatch, Long> {
}