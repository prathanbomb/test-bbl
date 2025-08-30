package com.example.testbbl.controller;

import com.example.testbbl.dto.PagedResult;
import com.example.testbbl.dto.PaginationInfo;
import com.example.testbbl.dto.UserDto;
import com.example.testbbl.exception.EmailAlreadyExistsException;
import com.example.testbbl.exception.GlobalExceptionHandler;
import com.example.testbbl.exception.UserNotFoundException;
import com.example.testbbl.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @Test
    void getUserById_returnsOk() {
        UserDto dto = new UserDto(1L, "Name", "username", "email@example.com", null, null);
        given(userService.getUserById(1L)).willReturn(Mono.just(dto));

        webTestClient.get()
                .uri("/users/{id}", 1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("Success")
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.email").isEqualTo("email@example.com");
    }

    @Test
    void getUserById_whenNotFound_returns404WithErrorBody() {
        given(userService.getUserById(99L)).willReturn(Mono.error(new UserNotFoundException("User not found with id: 99")));

        webTestClient.get()
                .uri("/users/{id}", 99)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(msg -> ((String) msg).contains("User not found"));
    }

    @Test
    void createUser_returns201() {
        UserDto output = new UserDto(10L, "Name", "username", "new@example.com", null, null);
        given(userService.createUser(any(UserDto.class))).willReturn(Mono.just(output));

        String body = "{" +
                "\"name\":\"Name\"," +
                "\"username\":\"username\"," +
                "\"email\":\"new@example.com\"" +
                "}";

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo(201)
                .jsonPath("$.message").isEqualTo("User created successfully")
                .jsonPath("$.data.id").isEqualTo(10)
                .jsonPath("$.data.email").isEqualTo("new@example.com");
    }

    @Test
    void createUser_whenEmailExists_returns409WithErrorBody() {
        given(userService.createUser(any(UserDto.class)))
                .willReturn(Mono.error(new EmailAlreadyExistsException("Email already exists: new@example.com")));

        String body = "{" +
                "\"name\":\"Name\"," +
                "\"username\":\"username\"," +
                "\"email\":\"new@example.com\"" +
                "}";

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message").value(msg -> ((String) msg).contains("Email already exists"));
    }

    @Test
    void createUser_whenValidationFails_returns400() {
        // missing name/username/email -> validation should fail
        String body = "{}";

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Validation failure");
    }

    @Test
    void getAllUsers_returnsPagedUsers() {
        UserDto user1 = new UserDto(1L, "User1", "user1", "user1@example.com", null, null);
        UserDto user2 = new UserDto(2L, "User2", "user2", "user2@example.com", null, null);
        List<UserDto> users = List.of(user1, user2);
        PaginationInfo pagination = PaginationInfo.of(0, 10, 2L);
        PagedResult<UserDto> pagedResult = new PagedResult<>(users, pagination);
        
        given(userService.getAllUsersWithPagination(0, 10)).willReturn(Mono.just(pagedResult));

        webTestClient.get()
                .uri("/users?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("Success")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.pagination.page").isEqualTo(0)
                .jsonPath("$.pagination.totalElements").isEqualTo(2);
    }

    @Test
    void getAllUsers_withInvalidPage_returns400() {
        webTestClient.get()
                .uri("/users?page=-1&size=0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }
}