package it.iet.model.repository;

import it.iet.infrastructure.mongo.entity.token.BaseToken;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import org.json.simple.parser.ParseException;

import java.util.Optional;

public interface BaseTokenRepository {

    boolean existsByContentAndType(String token, TokenType refresh);
    Optional<BaseToken> findByContentAndType(String token, TokenType refresh);
    void saveToken(String content, String associatedWith, TokenType type);
    BaseToken updateRefreshToken(String oldRefreshToken, String newAccesstoken, String oldAccessToken) throws ParseException;

    void deleteTokenByContent(String content);
}
