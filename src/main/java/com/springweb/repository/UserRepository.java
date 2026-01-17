package com.springweb.repository;

import com.springweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

       Optional<User> findByUsername(String username);

       Optional<User> findByUsernameAndAccessCode(String username, String accessCode);

       boolean existsByUsername(String username);
}