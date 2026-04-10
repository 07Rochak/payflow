package com.rochak.payflow.service;

import com.rochak.payflow.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO user);

    List<UserDTO> getAllUsers();

    UserDTO getUserByEmail(String email);
}
