package it.iet.infrastructure.mongo.repository.user;

import it.iet.infrastructure.mongo.entity.user.User;
import it.iet.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InternalUserRepository implements UserRepository {

    @Autowired
    private UserMongoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findUserByEmail(email);
    }

    @Override
    public User save(User u) {
        return repository.save(u);
    }

    @Override
    public Optional<User> findbyId(String id) {
        return repository.findBy_id(id);
    }

    @Override
    public List<User> findAllUsers() {
        return repository.findAll();
    }

    @Override
    public void deleteUser(User u) {
        repository.delete(u);
    }

    @Override
    public Page<User> findAllUsersPaged(Pageable paging) {
        return repository.findAll(paging);
    }
}
