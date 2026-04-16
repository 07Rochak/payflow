package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.UserDTO;
import com.rochak.payflow.entity.User;
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
    public UserDTO createUser(UserDTO userDTO) {
        User user = mapToUser(userDTO);
        if(userRepository.findByEmail(userDTO.getEmail())!=null){
            throw new RuntimeException("Email already exists");
        }
        User savedUser = userRepository.save(user);
        UserDTO savedUserDto = mapToUserDto(savedUser);
        return savedUserDto;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users= userRepository.findAll();
        return users.stream()
                .map((user) -> mapToUserDto(user))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return mapToUserDto(user);
    }

    private UserDTO mapToUserDto(User user){
        return new UserDTO(user.getName(), user.getEmail(), user.getPassword());
    }

    private User mapToUser(UserDTO userDTO){
        User user = new User();
        user.setEmail(userDTO.getName());
        user.setName(userDTO.getEmail());
        user.setPassword(user.getPassword());
        return user;
    }
}
