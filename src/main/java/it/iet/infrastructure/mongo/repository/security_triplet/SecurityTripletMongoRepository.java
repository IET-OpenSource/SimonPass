package it.iet.infrastructure.mongo.repository.security_triplet;

import it.iet.infrastructure.mongo.entity.SecurityTriplet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SecurityTripletMongoRepository extends MongoRepository<SecurityTriplet, String> {
    List<SecurityTriplet> findByUserIdAndToken(String userId, String token);
    Optional<SecurityTriplet> findByUserIdAndTokenAndIdentifier(String userId, String token, String identifier);
    void deleteAllByUserIdAndTokenAndIdentifier(String userId, String token, String identifier);
    void deleteSecurityTripletByToken(String token);
}
