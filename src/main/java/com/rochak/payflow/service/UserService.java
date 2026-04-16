package com.rochak.payflow.service;

import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO user);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);

    void deleteUser(Long id);
}
