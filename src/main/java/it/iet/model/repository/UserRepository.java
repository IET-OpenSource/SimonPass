package it.iet.model.repository;

import it.iet.infrastructure.mongo.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    User save(User u);

    Optional<User> findbyId(String id);

    List<User> findAllUsers();

    void deleteUser(User u);

    Page<User> findAllUsersPaged(Pageable paging);
}
