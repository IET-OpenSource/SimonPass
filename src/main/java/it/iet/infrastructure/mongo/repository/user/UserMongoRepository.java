package it.iet.infrastructure.mongo.repository.user;

import it.iet.infrastructure.mongo.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<User, String> {

    Optional<User> findUserByEmail(String email);

    Optional<User> findBy_id(String id);

    Page<User> findAll(Pageable pageable);

}
