package com.pictManager.repository;

import org.springframework.stereotype.Repository;

import com.pictManager.model.User;

import org.springframework.data.repository.CrudRepository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmail(String email);

    User findByUsername(String username);

    User findByEmailAndPassword(String email, String password);
}
