package com.example.testbbl.controller;

import com.example.testbbl.dto.ApiResponse;
import com.example.testbbl.dto.UserDto;
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
    public Mono<ApiResponse<List<UserDto>>> getAllUsers(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size) {
        return userService.getAllUsersWithPagination(page, size)
                .map(result -> ApiResponse.success(result.getData(), result.getPagination()));
    }

    @GetMapping("/users/{id}")
    public Mono<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ApiResponse::success);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto)
                .map(user -> ApiResponse.<UserDto>builder()
                        .status(201)
                        .message("User created successfully")
                        .data(user)
                        .build());
    }

    @PutMapping("/users/{id}")
    public Mono<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        return userService.updateUser(id, userDto)
                .map(user -> ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public Mono<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ApiResponse.<Void>builder()
                        .status(204)
                        .message("User deleted successfully")
                        .build()));
    }

}
