package com.rochak.payflow.mapper;

import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.entity.User;

public class UserMapper {
    public static User mapToNewEntity(CreateUserRequestDTO userRequestDTO)
    {
        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        return user;
    }

    public static User mapToEntity(UserRequestDTO userRequestDTO){
        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        return user;
    }

    public static UserResponseDTO mapToResponse(User user){
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
