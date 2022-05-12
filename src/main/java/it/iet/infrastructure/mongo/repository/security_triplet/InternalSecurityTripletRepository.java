package it.iet.infrastructure.mongo.repository.security_triplet;

import it.iet.infrastructure.mongo.entity.SecurityTriplet;
import it.iet.model.repository.SecurityTripletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InternalSecurityTripletRepository implements SecurityTripletRepository {

    @Autowired
    private SecurityTripletMongoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public SecurityTriplet createTriplet(String username, String token, String identifier) {
        var securityTriplet = new SecurityTriplet(username, token, identifier);
        return repository.save(securityTriplet);
    }

    @Override
    public List<SecurityTriplet> retrieveTriplets(String username, String token) {
        return repository.findByUserIdAndToken(username, token);
    }

    @Override
    public Optional<SecurityTriplet> retrieveSingleTriplet(String usr, String token, String identifier) {
        return repository.findByUserIdAndTokenAndIdentifier(usr, token, identifier);
    }

    @Override
    public void deleteAll(String usr, String identifier, String token) {
        repository.deleteAllByUserIdAndTokenAndIdentifier(usr, identifier, token);
    }

    @Override
    public void deleteByToken(String token) {
        repository.deleteSecurityTripletByToken(token);
    }
}
