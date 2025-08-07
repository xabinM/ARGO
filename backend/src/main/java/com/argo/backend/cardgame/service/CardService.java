package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.card.CardInfoResponse;
import com.argo.backend.cardgame.exception.types.CardNotFoundException;
import com.argo.backend.cardgame.exception.types.CardValidationException;
import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {
    
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    
    /**
     * 카드 기본 정보 조회 (유효성 검증 목적)
     * 클라이언트에서는 매퍼로 처리하므로, 서버에서는 cardId 유효성 검증만 수행
     */
    public CardInfoResponse getCardInfo(Long cardId, Long userId) {
        validateUser(userId);
        
        if (cardId == null || cardId <= 0) {
            throw new CardValidationException("유효하지 않은 카드 ID입니다.");
        }
        
        boolean exists = cardRepository.existsById(cardId);
        
        return CardInfoResponse.of(cardId, exists);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException();
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

}