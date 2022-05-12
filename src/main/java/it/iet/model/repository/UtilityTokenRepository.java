package it.iet.model.repository;

import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.entity.token.UtilityToken;

import java.util.Optional;

public interface UtilityTokenRepository {

    void save(UtilityToken utilityToken);
    Optional<UtilityToken> findByToken(String token);
    Optional<UtilityToken> findByTokenAndType(String token, TokenType type);

    void deleteToken(String token, TokenType type);
}
