package it.iet.infrastructure.mongo.repository.token;

import it.iet.infrastructure.mongo.entity.token.BaseToken;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.model.repository.BaseTokenRepository;
import it.iet.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InternalBaseTokenRepository implements BaseTokenRepository {

    @Autowired
    private BaseTokenMongoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean existsByContentAndType(String token, TokenType refresh) {
        return repository.findByContentAndType(token, refresh).isPresent();
    }

    @Override
    public Optional<BaseToken> findByContentAndType(String token, TokenType refresh) {
        return repository.findByContentAndType(token, refresh);
    }

    @Override
    public void saveToken(String content, String associatedWith, TokenType type) {
        var token = new BaseToken();
        token.setType(type);
        token.setTokenAssociated(associatedWith);
        token.setContent(content);
        token.set_id(Utils.generateCryptId());
        repository.save(token);
    }

    @Override
    public BaseToken updateRefreshToken(String oldRefreshToken, String newAccesstoken, String oldAccessToken) {
        deleteOldToken(oldAccessToken, TokenType.ACCESS);
        Optional<BaseToken> refreshTokenFromDb = repository.findByContentAndTypeAndTokenAssociated(oldRefreshToken, TokenType.REFRESH, oldAccessToken);
        if (refreshTokenFromDb.isPresent()) {
            var baseToken = refreshTokenFromDb.get();
            baseToken.setTokenAssociated(newAccesstoken);
            return repository.save(baseToken);
        }
        return null;
    }

    @Override
    public void deleteTokenByContent(String content) {
        repository.deleteBaseTokenByContent(content);
    }

    private void deleteOldToken(String oldAccessToken, TokenType type) {
        Optional<BaseToken> del = repository.findByContentAndType(oldAccessToken, type);
        if (del.isPresent()) {
            BaseToken tokenToBeDeleted = del.get();
            repository.delete(tokenToBeDeleted);
        }
    }
}
