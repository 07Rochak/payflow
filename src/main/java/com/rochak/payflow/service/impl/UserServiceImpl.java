package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.ChangePasswordDto;
import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.entity.Wallet;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.mapper.UserMapper;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private WalletRepository walletRepository;

//    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder){
//        this.passwordEncoder=passwordEncoder;
//            }

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO userDTO) {
        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        User user = UserMapper.mapToNewEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(0.0);
        walletRepository.save(wallet);
        UserResponseDTO savedUserDto = UserMapper.mapToResponse(savedUser);
        return savedUserDto;
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No user with id: "+id)
                );
        return UserMapper.mapToResponse(user);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        return UserMapper.mapToResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No user with id: "+id)
                );
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        User savedUser = userRepository.save(user);
        UserResponseDTO userResponseDTO = UserMapper.mapToResponse(savedUser);
        return userResponseDTO;
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No user with id: "+id)
                );
        userRepository.delete(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No user with id: "+userId)
                );
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
