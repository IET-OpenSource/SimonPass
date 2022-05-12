package it.iet.model.repository;

import it.iet.infrastructure.mongo.entity.SecurityTriplet;

import java.util.List;
import java.util.Optional;

public interface SecurityTripletRepository {
    SecurityTriplet createTriplet(String username, String token, String identifier);
    List<SecurityTriplet> retrieveTriplets(String username, String token);
    Optional<SecurityTriplet> retrieveSingleTriplet(String usr, String token, String identifier);
    void deleteAll(String usr, String identifier, String token);
    void deleteByToken(String token);
}
