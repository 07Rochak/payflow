package com.rochak.payflow.mapper;

import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.entity.User;

public class UserMapper {
    public static User mapToEntity(UserRequestDTO userRequestDTO)
    {
        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(userRequestDTO.getPassword());
        return user;
    }

    public static UserResponseDTO mapToResponse(User user){
        return new UserResponseDTO(user.getId(), user.getName(), user.getPassword());
    }
}
