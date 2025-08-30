package com.example.testbbl.repository;

import com.example.testbbl.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<Boolean> existsByEmailIgnoreCase(String email);
    Mono<User> findByEmailIgnoreCase(String email);
}