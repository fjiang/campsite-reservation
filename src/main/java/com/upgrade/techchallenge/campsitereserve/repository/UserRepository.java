package com.upgrade.techchallenge.campsitereserve.repository;

import com.upgrade.techchallenge.campsitereserve.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    User findOneByEmail(String email);

    Optional<User> findById(Long id);
}
