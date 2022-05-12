package it.iet;

import it.iet.infrastructure.mongo.entity.token.BaseToken;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.repository.token.BaseTokenMongoRepository;
import it.iet.util.Utils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TokenUtilityTest {

    @Autowired
    private BaseTokenMongoRepository repository;

    @Test
    @Order(1)
    @Disabled
    void contextLoads() {

    }

    @Test
    @Order(2)
    @Disabled
    void createTokens() {
        String content = "QqfPlV9VdOzb6MZtji";
        BaseToken publicRequestToken = new BaseToken();
        publicRequestToken.setContent(content);
        publicRequestToken.setType(TokenType.PUBLIC_REQUEST);
        publicRequestToken.set_id(Utils.generateCryptId());
        repository.save(publicRequestToken);
        Optional<BaseToken> result = repository.findByContentAndType(content, TokenType.PUBLIC_REQUEST);
        result.ifPresent(Assertions::assertNotNull);
    }

    @Test
    @Order(3)
    @Disabled
    void deleteAllTokens() {
        // we delete tokens logically without removing them from DB
        List<BaseToken> tokens = repository.findAll();
        for (BaseToken t : tokens) {
            t.setDeleted(new Date());
            repository.save(t);
        }
        tokens = repository.findAll();
        Assertions.assertTrue(tokens.isEmpty());
    }

}
