package it.iet.config.security;

import it.iet.infrastructure.mongo.entity.token.BaseToken;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.repository.token.BaseTokenMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class PublicRequestTokenValidator {

    @Autowired
    private BaseTokenMongoRepository repository;

    public boolean isValidToken(String token) {
        List<BaseToken> tokenSignUpList = repository.findAll();
        for (BaseToken t : tokenSignUpList) {
            if (t.getContent().equals(token) && t.getType().equals(TokenType.PUBLIC_REQUEST)) {
                return true;
            }
        }
        return false;
    }
}
