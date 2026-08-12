package com.rochak.payflow.service.impl;

import com.rochak.payflow.dto.auth.*;
import com.rochak.payflow.dto.request.*;
import com.rochak.payflow.entity.*;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.security.jwt.JwtService;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.session.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock SessionService sessionService;
    @Mock DeviceExtractor deviceExtractor;
    @Mock IpExtractor ipExtractor;
    @Mock HttpServletRequest request;

    private AuthServiceImpl service() { return new AuthServiceImpl(userRepository,passwordEncoder,jwtService,sessionService,deviceExtractor,ipExtractor); }

    @Test void login_shouldCreateSessionAndTokens() {
        User user=new User(1L,"a@b.com","A","encoded",Role.USER);
        UserSession session=UserSession.builder().sessionId("s1").currentTokenId("t1").build();
        LoginRequestDTO req=new LoginRequestDTO(); req.setEmail("a@b.com"); req.setPassword("password");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password","encoded")).thenReturn(true);
        when(deviceExtractor.extract(request)).thenReturn("Chrome"); when(ipExtractor.extract(request)).thenReturn("127.0.0.1");
        when(sessionService.createSession(user,"Chrome","127.0.0.1")).thenReturn(session);
        when(jwtService.generateToken("a@b.com","USER")).thenReturn("access");
        when(jwtService.generateRefreshToken("a@b.com","s1","t1")).thenReturn("refresh");
        AuthResponseDTO response=service().login(req,request);
        assertEquals("access",response.getAccessToken()); assertEquals("refresh",response.getRefreshToken());
    }

    @Test void login_shouldRejectBadPassword() {
        User user=new User(1L,"a@b.com","A","encoded",Role.USER);
        LoginRequestDTO req=new LoginRequestDTO(); req.setEmail("a@b.com"); req.setPassword("bad");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad","encoded")).thenReturn(false);
        assertThrows(RuntimeException.class,()->service().login(req,request));
        verifyNoInteractions(sessionService);
    }

    @Test void logout_shouldDeleteSessionForValidRefreshToken() {
        LogoutRequestDto req=new LogoutRequestDto(); req.setRefreshToken("refresh");
        when(jwtService.isTokenValid("refresh")).thenReturn(true); when(jwtService.extractSessionId("refresh")).thenReturn("s1"); when(jwtService.extractEmail("refresh")).thenReturn("a@b.com");
        service().logout(req); verify(sessionService).deleteSession("s1");
    }

    @Test void logout_shouldDoNothingForInvalidToken() {
        LogoutRequestDto req=new LogoutRequestDto(); req.setRefreshToken("bad"); when(jwtService.isTokenValid("bad")).thenReturn(false);
        service().logout(req); verifyNoInteractions(sessionService);
    }
}
