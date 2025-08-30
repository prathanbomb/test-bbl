package com.example.testbbl.service;

import com.example.testbbl.dto.UserDto;
import com.example.testbbl.exception.EmailAlreadyExistsException;
import com.example.testbbl.exception.UserNotFoundException;
import com.example.testbbl.mapper.UserMapper;
import com.example.testbbl.model.User;
import com.example.testbbl.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserById_whenFound_returnsDto() {
        User user = new User(1L, "Name", "username", "email@example.com", null, null);
        UserDto dto = new UserDto(1L, "Name", "username", "email@example.com", null, null);

        when(userRepository.findById(1L)).thenReturn(Mono.just(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        StepVerifier.create(userService.getUserById(1L))
                .expectNext(dto)
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_whenNotFound_errorsWithUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(99L))
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof UserNotFoundException;
                })
                .verify();

        verify(userRepository).findById(99L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void createUser_whenEmailExists_errorsWithConflict() {
        UserDto input = new UserDto(null, "Name", "username", "exists@example.com", null, null);
        when(userRepository.existsByEmailIgnoreCase("exists@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.createUser(input))
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof EmailAlreadyExistsException;
                })
                .verify();

        verify(userRepository).existsByEmailIgnoreCase("exists@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void createUser_whenValid_savesAndReturnsDto() {
        UserDto input = new UserDto(null, "Name", "username", "new@example.com", null, null);
        User toSave = new User(null, "Name", "username", "new@example.com", null, null);
        User saved = new User(10L, "Name", "username", "new@example.com", null, null);
        UserDto output = new UserDto(10L, "Name", "username", "new@example.com", null, null);

        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(Mono.just(false));
        when(userMapper.toEntity(input)).thenReturn(toSave);
        when(userRepository.save(toSave)).thenReturn(Mono.just(saved));
        when(userMapper.toDto(saved)).thenReturn(output);

        StepVerifier.create(userService.createUser(input))
                .expectNext(output)
                .verifyComplete();

        verify(userRepository).existsByEmailIgnoreCase("new@example.com");
        verify(userMapper).toEntity(input);
        verify(userRepository).save(toSave);
        verify(userMapper).toDto(saved);
    }

    @Test
    void deleteUser_whenExists_deletes() {
        when(userRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenNotExists_errorsWithNotFound() {
        when(userRepository.existsById(123L)).thenReturn(Mono.just(false));

        StepVerifier.create(userService.deleteUser(123L))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).existsById(123L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
