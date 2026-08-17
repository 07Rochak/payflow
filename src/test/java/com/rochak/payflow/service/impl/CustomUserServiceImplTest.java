package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserServiceImpl service() {
        return new CustomUserServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {

        User user = new User(
                1L,
                "a@b.com",
                "A",
                "encoded-password",
                Role.USER
        );

        when(userRepository.findByEmail("a@b.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                service().loadUserByUsername("a@b.com");

        assertEquals("a@b.com", result.getUsername());
        assertEquals("encoded-password", result.getPassword());

        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals("ROLE_USER")
                        )
        );
    }

    @Test
    void loadUserByUsername_shouldThrowWhenUserMissing() {

        when(userRepository.findByEmail("missing@b.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service().loadUserByUsername("missing@b.com")
        );
    }
}