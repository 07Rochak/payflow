package com.rochak.payflow.service;

import com.rochak.payflow.dto.ChangePasswordDto;
import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(CreateUserRequestDTO user);

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);

    void deleteUser(Long id);

    void changePassword(Long userId, ChangePasswordDto request);
}
