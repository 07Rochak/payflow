package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.mapper.UserMapper;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Override
    public UserResponseDTO createUser(UserRequestDTO userDTO) {
        if(userRepository.findByEmail(userDTO.getEmail())!=null){
            throw new RuntimeException("Email already exists");
        }
        User user = UserMapper.mapToEntity(userDTO);
        User savedUser = userRepository.save(user);
        UserResponseDTO savedUserDto = UserMapper.mapToResponse(savedUser);
        return savedUserDto;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> users= userRepository.findAll();
        return users.stream()
                .map((user) -> UserMapper.mapToResponse(user))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return UserMapper.mapToResponse(user);
    }
}
