package com.example.testbbl.service;

import com.example.testbbl.dto.UserDto;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final com.example.testbbl.mapper.UserMapper userMapper;

    public Flux<UserDto> getAllUsers(int page, int size) {
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        long skip = (long) pageable.getPageNumber() * pageable.getPageSize();
        return userRepository.findAll()
                .skip(skip)
                .take(pageable.getPageSize())
                .map(userMapper::toDto);
    }

    public Mono<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)));
    }

    public Mono<UserDto> createUser(UserDto userDto) {
        return userRepository.existsByEmailIgnoreCase(userDto.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.<User>error(new EmailAlreadyExistsException("Email already exists: " + userDto.getEmail()));
                    }
                    User entity = userMapper.toEntity(userDto);
                    entity.setId(null); // ensure new entity
                    return userRepository.save(entity);
                })
                .map(userMapper::toDto)
                .onErrorMap(err -> (err instanceof DuplicateKeyException || err instanceof DataIntegrityViolationException),
                        err -> new EmailAlreadyExistsException("Email already exists: " + userDto.getEmail()));
    }

    public Mono<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)))
                .flatMap(existing -> {
                    String newEmail = userDto.getEmail();
                    if (newEmail != null && !newEmail.equalsIgnoreCase(existing.getEmail())) {
                        return userRepository.findByEmailIgnoreCase(newEmail)
                                .flatMap(found -> !found.getId().equals(id)
                                        ? Mono.<User>error(new EmailAlreadyExistsException("Email already exists: " + newEmail))
                                        : Mono.just(existing))
                                .switchIfEmpty(Mono.just(existing))
                                .flatMap(user -> {
                                    userMapper.updateEntityFromDto(userDto, user);
                                    return userRepository.save(user);
                                });
                    } else {
                        userMapper.updateEntityFromDto(userDto, existing);
                        return userRepository.save(existing);
                    }
                })
                .map(userMapper::toDto)
                .onErrorMap(err -> (err instanceof DuplicateKeyException || err instanceof DataIntegrityViolationException),
                        err -> new EmailAlreadyExistsException("Email already exists: " + userDto.getEmail()));
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.existsById(id)
                .flatMap(exists -> exists
                        ? userRepository.deleteById(id)
                        : Mono.error(new UserNotFoundException("User not found with id: " + id))
                );
    }

}
