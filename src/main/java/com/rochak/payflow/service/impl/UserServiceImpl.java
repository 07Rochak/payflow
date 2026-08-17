package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.ChangePasswordDto;
import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.entity.Wallet;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.mapper.UserMapper;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private WalletRepository walletRepository;

    private SessionService sessionService;

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
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(0.0);
        walletRepository.save(wallet);
        UserResponseDTO savedUserDto = UserMapper.mapToResponse(savedUser);
        return savedUserDto;
    }

    @Override
    public UserResponseDTO createAdminUser(CreateUserRequestDTO userDTO) {
        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        User user = UserMapper.mapToNewEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.ADMIN);
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

//    @Override
//    public UserResponseDTO updateUser(String email, UserRequestDTO userRequestDTO) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(
//                        () -> new ResourceNotFoundException("No user with email: "+email)
//                );
//        if (!email.equals(userRequestDTO.getEmail()) && userRepository.existsByEmail(userRequestDTO.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//        user.setName(userRequestDTO.getName());
//        user.setEmail(userRequestDTO.getEmail());
//        User savedUser = userRepository.save(user);
//        UserResponseDTO userResponseDTO = UserMapper.mapToResponse(savedUser);
//        return userResponseDTO;
//    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(
            String currentEmail,
            UserRequestDTO userRequestDTO) {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No user with email: " + currentEmail
                        )
                );

        String newEmail = userRequestDTO.getEmail();

        // Check whether the requested email is already used
        // by another user.
        if (!currentEmail.equals(newEmail)
                && userRepository.existsByEmail(newEmail)) {

            throw new RuntimeException("Email already exists");
        }

        boolean emailChanged = !currentEmail.equals(newEmail);

        user.setName(userRequestDTO.getName());
        user.setEmail(newEmail);

        User savedUser = userRepository.save(user);

        // Email is part of the authentication identity.
        // Invalidate all existing sessions so the user must
        // authenticate again with the new email.
        if (emailChanged) {
            sessionService.deleteAllSessions(savedUser.getId());
        }

        return UserMapper.mapToResponse(savedUser);
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
    public void changePassword(String email, ChangePasswordDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No user with email: "+email)
                );
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public User getUserByEmailId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        return user;
    }

}
