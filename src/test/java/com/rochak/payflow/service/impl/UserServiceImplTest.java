package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock WalletRepository walletRepository;
    @Mock SessionService sessionService;
    @InjectMocks UserServiceImpl service;

    @Test
    void createUser_shouldSaveUserAndWallet() {
        CreateUserRequestDTO request = new CreateUserRequestDTO("Test", "test@example.com", "password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> { User u=inv.getArgument(0); u.setId(1L); return u; });
        var response = service.createUser(request);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        verify(walletRepository).save(argThat(w -> w.getUser().getId().equals(1L) && w.getBalance() == 0.0));
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        CreateUserRequestDTO request = new CreateUserRequestDTO("Test", "test@example.com", "password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnUser() {
        User u = new User(1L, "a@b.com", "A", "p", Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        assertEquals(1L, service.getUserById(1L).getId());
    }

    @Test
    void getUserById_shouldThrowWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getUserById(1L));
    }

    @Test
    void getAllUsers_shouldReturnUsers() {
        when(userRepository.findAll()).thenReturn(List.of(new User(1L,"a@b.com","A","p",Role.USER)));
        assertEquals(1, service.getAllUsers().size());
    }

    @Test
    void updateUser_shouldUpdateFields() {
        User u = new User(1L, "old@b.com", "Old","p", Role.USER);
        when(userRepository.findByEmail("old@b.com")).thenReturn(Optional.of(u));
        when(userRepository.save(u)).thenReturn(u);
        var response = service.updateUser("old@b.com", new UserRequestDTO("New", "new@b.com"));
        assertEquals("New", response.getName());
        assertEquals("new@b.com", response.getEmail());
    }

    @Test
    void deleteUser_shouldDeleteExistingUser() {
        User u = new User(1L,"a@b.com","A","p",Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        service.deleteUser(1L);
        verify(userRepository).delete(u);
    }
}
