package it.iet.infrastructure.mongo.repository.token;

import it.iet.infrastructure.mongo.entity.token.BaseToken;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BaseTokenMongoRepository extends MongoRepository<BaseToken, String> {

    Optional<BaseToken> findByContentAndType(String content, TokenType type);

    Optional<BaseToken> findByContentAndTypeAndTokenAssociated(String content, TokenType type, String tokenAssociated);

    void deleteBaseTokenByContent(String content);
}
