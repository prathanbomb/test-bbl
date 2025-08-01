package com.example.testbbl.service;

import com.example.testbbl.dto.UserDto;
import com.example.testbbl.exception.UserNotFoundException;
import com.example.testbbl.model.User;
import com.example.testbbl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Flux<UserDto> getAllUsers() {
        return userRepository.findAll()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getWebsite()
                ));
    }

    public Mono<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getWebsite()
                )).switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)));
    }

    public Mono<UserDto> createUser(UserDto userDto) {
        return userRepository.save(new User(
                null,
                userDto.getName(),
                userDto.getUsername(),
                userDto.getEmail(),
                userDto.getPhone(),
                userDto.getWebsite()
        )).map(user -> new UserDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getWebsite()
        ));
    }

    public Mono<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setName(userDto.getName());
                    user.setUsername(userDto.getUsername());
                    user.setEmail(userDto.getEmail());
                    user.setPhone(userDto.getPhone());
                    user.setWebsite(userDto.getWebsite());
                    return userRepository.save(user);
                })
                .map(user -> new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getWebsite()
                ));
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.deleteById(id);
    }

}
