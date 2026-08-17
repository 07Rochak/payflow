package com.rochak.payflow.service;

import com.rochak.payflow.dto.ChangePasswordDto;
import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.entity.User;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(CreateUserRequestDTO user);

    UserResponseDTO createAdminUser(CreateUserRequestDTO user);

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO updateUser(String email, UserRequestDTO userRequestDTO);

    void deleteUser(Long id);

    void changePassword(String email, ChangePasswordDto request);

    User getUserByEmailId(String email);
}
