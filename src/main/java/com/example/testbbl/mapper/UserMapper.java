package com.example.testbbl.mapper;

import com.example.testbbl.dto.request.CreateUserRequest;
import com.example.testbbl.dto.request.UpdateUserRequest;
import com.example.testbbl.dto.response.UserResponse;
import com.example.testbbl.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User entity);
}
