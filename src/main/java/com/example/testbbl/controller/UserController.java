package com.example.testbbl.controller;

import com.example.testbbl.dto.ApiResponse;
import com.example.testbbl.dto.request.CreateUserRequest;
import com.example.testbbl.dto.request.UpdateUserRequest;
import com.example.testbbl.dto.response.UserResponse;
import com.example.testbbl.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Validated
class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public Mono<ApiResponse<List<UserResponse>>> getAllUsers(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size) {
        return userService.getAllUsersWithPagination(page, size)
                .map(result -> ApiResponse.success(result.getData(), result.getPagination()));
    }

    @GetMapping("/users/{id}")
    public Mono<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ApiResponse::success);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request)
                .map(user -> ApiResponse.<UserResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("User created successfully")
                        .data(user)
                        .build());
    }

    @PutMapping("/users/{id}")
    public Mono<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request)
                .map(user -> ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public Mono<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ApiResponse.<Void>builder()
                        .status(HttpStatus.NO_CONTENT.value())
                        .message("User deleted successfully")
                        .build()));
    }

}
