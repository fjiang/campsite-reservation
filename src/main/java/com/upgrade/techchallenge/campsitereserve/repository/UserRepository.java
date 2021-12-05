package com.upgrade.techchallenge.campsitereserve.repository;

import com.upgrade.techchallenge.campsitereserve.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findByLastName(String lastName);

    Optional<User> findById(Long id);
}
