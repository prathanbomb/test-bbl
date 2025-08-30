package com.example.testbbl.controller;

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
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.email").isEqualTo("email@example.com");
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
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").value(msg -> ((String) msg).contains("User not found"))
                .jsonPath("$.path").isEqualTo("/users/99");
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
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.email").isEqualTo("new@example.com");
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
                .jsonPath("$.error").isEqualTo("Conflict")
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
                .jsonPath("$.message").isEqualTo("Validation failed");
    }

    @Test
    void getAllUsers_withInvalidPage_returns400() {
        given(userService.getAllUsers(anyInt(), anyInt())).willReturn(Flux.empty());

        webTestClient.get()
                .uri("/users/page/{page}/size/{size}", -1, 0)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }
}