package it.iet.infrastructure.mongo.repository.token;

import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.entity.token.UtilityToken;
import it.iet.model.repository.UtilityTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InternalUtilityTokenRepository implements UtilityTokenRepository {

    @Autowired
    private UtilityTokenMongoRepository repository;

    @Override
    public void save(UtilityToken utilityToken) {
        repository.save(utilityToken);
    }

    @Override
    public Optional<UtilityToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    @Override
    public Optional<UtilityToken> findByTokenAndType(String token, TokenType type) {
        return repository.findByTokenAndType(token, type);
    }

    @Override
    public void deleteToken(String token, TokenType type) {
        repository.deleteByTokenAndType(token, type);
    }
}
