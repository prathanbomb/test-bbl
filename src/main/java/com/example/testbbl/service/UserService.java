package com.example.testbbl.service;

import com.example.testbbl.dto.PagedResult;
import com.example.testbbl.dto.PaginationInfo;
import com.example.testbbl.dto.request.CreateUserRequest;
import com.example.testbbl.dto.request.UpdateUserRequest;
import com.example.testbbl.dto.response.UserResponse;
import com.example.testbbl.exception.EmailAlreadyExistsException;
import com.example.testbbl.exception.UserNotFoundException;
import com.example.testbbl.model.User;
import com.example.testbbl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final com.example.testbbl.mapper.UserMapper userMapper;

    public Flux<UserResponse> getAllUsers(int page, int size) {
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        long skip = (long) pageable.getPageNumber() * pageable.getPageSize();
        return userRepository.findAll()
                .skip(skip)
                .take(pageable.getPageSize())
                .map(userMapper::toResponse);
    }

    public Mono<PagedResult<UserResponse>> getAllUsersWithPagination(int page, int size) {
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        
        return userRepository.count()
                .flatMap(totalElements -> {
                    PaginationInfo pagination = PaginationInfo.of(safePage, safeSize, totalElements);
                    
                    return getAllUsers(safePage, safeSize)
                            .collectList()
                            .map(users -> new PagedResult<>(users, pagination));
                });
    }

    public Mono<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)));
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return userRepository.existsByEmailIgnoreCase(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.<User>error(new EmailAlreadyExistsException("Email already exists: " + request.getEmail()));
                    }
                    User entity = userMapper.toEntity(request);
                    entity.setId(null); // ensure new entity
                    return userRepository.save(entity);
                })
                .map(userMapper::toResponse)
                .onErrorMap(err -> (err instanceof DuplicateKeyException || err instanceof DataIntegrityViolationException),
                        err -> new EmailAlreadyExistsException("Email already exists: " + request.getEmail()));
    }

    public Mono<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)))
                .flatMap(existing -> {
                    String newEmail = request.getEmail();
                    if (newEmail != null && !newEmail.equalsIgnoreCase(existing.getEmail())) {
                        return userRepository.findByEmailIgnoreCase(newEmail)
                                .flatMap(found -> !found.getId().equals(id)
                                        ? Mono.<User>error(new EmailAlreadyExistsException("Email already exists: " + newEmail))
                                        : Mono.just(existing))
                                .switchIfEmpty(Mono.just(existing))
                                .flatMap(user -> {
                                    userMapper.updateEntityFromRequest(request, user);
                                    return userRepository.save(user);
                                });
                    } else {
                        userMapper.updateEntityFromRequest(request, existing);
                        return userRepository.save(existing);
                    }
                })
                .map(userMapper::toResponse)
                .onErrorMap(err -> (err instanceof DuplicateKeyException || err instanceof DataIntegrityViolationException),
                        err -> new EmailAlreadyExistsException("Email already exists: " + request.getEmail()));
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.existsById(id)
                .flatMap(exists -> exists
                        ? userRepository.deleteById(id)
                        : Mono.error(new UserNotFoundException("User not found with id: " + id))
                );
    }

}
