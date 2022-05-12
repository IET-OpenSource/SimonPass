package it.iet.infrastructure.mongo.repository.token;

import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.entity.token.UtilityToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UtilityTokenMongoRepository extends MongoRepository<UtilityToken, String> {

    Optional<UtilityToken> findByToken(String token);

    Optional<UtilityToken> findByTokenAndType(String token, TokenType type);

    void deleteByTokenAndType(String token, TokenType type);
}
