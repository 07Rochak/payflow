package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.*;
import com.rochak.payflow.exception.*;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.service.SessionValidationService;
import com.rochak.payflow.session.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {
    @Mock UserSessionRepository repository;
    @Mock SessionProperties properties;
    @Mock RedisTemplate<String,String> redisTemplate;
    @Mock SessionValidationService validationService;
    @Mock DeviceExtractor deviceExtractor;
    @Mock IpExtractor ipExtractor;
    @Mock SetOperations<String,String> setOperations;
    @Mock HttpServletRequest request;
    private SessionServiceImpl service;

    @BeforeEach void setUp(){
        service=new SessionServiceImpl(repository,properties,redisTemplate,validationService,deviceExtractor,ipExtractor);
    }

    @Test void createSession_shouldPersistAndIndexSession(){
        when(properties.getMaxSessionLifetime()).thenReturn(3600L);
        when(properties.getRefreshTokenTtl()).thenReturn(604800L);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        User user=new User(1L,"a@b.com","A","p",Role.USER);
        when(repository.save(any(UserSession.class))).thenAnswer(inv->inv.getArgument(0));
        UserSession result=service.createSession(user,"Chrome","127.0.0.1");
        assertEquals(1L,result.getUserId()); assertEquals("a@b.com",result.getEmail());
        assertEquals("a@b.com", result.getEmail());
        assertEquals(1,result.getSessionVersion());
        verify(setOperations).add(eq(RedisKeys.userSessions(1L)),eq(result.getSessionId()));
    }

    @Test void getRequiredSession_shouldReturnSession(){
        UserSession s=UserSession.builder().sessionId("s1").build();
        when(repository.findById("s1")).thenReturn(Optional.of(s));
        assertSame(s,service.getRequiredSession("s1").orElseThrow());
        verify(repository).findById("s1");
    }

    @Test void deleteSession_shouldDeleteAndRemoveIndex(){
        UserSession s=UserSession.builder().sessionId("s1").userId(1L).build();
        when(repository.findById("s1")).thenReturn(Optional.of(s));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        service.deleteSession("s1"); verify(repository).deleteById("s1");
        verify(setOperations).remove(RedisKeys.userSessions(1L),"s1");
    }

    @Test void deleteAllSessions_shouldDeleteAllAndIndex(){
        when(setOperations.members(RedisKeys.userSessions(1L))).thenReturn(Set.of("s1","s2"));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        service.deleteAllSessions(1L); verify(repository).deleteById("s1");
        verify(repository).deleteById("s2"); verify(redisTemplate).delete(RedisKeys.userSessions(1L));
    }

    @Test void validateAndRotate_shouldRotateToken(){
        UserSession s=UserSession.builder().sessionId("s1").userId(1L).currentTokenId("t1").sessionVersion(1).expiresAt(Instant.now().plusSeconds(300)).build();
        when(repository.findById("s1")).thenReturn(Optional.of(s)); when(deviceExtractor.extract(request)).thenReturn("Chrome"); when(ipExtractor.extract(request)).thenReturn("127.0.0.1"); when(repository.save(any(UserSession.class))).thenAnswer(inv->inv.getArgument(0));
        UserSession result=service.validateAndRotate("s1","t1",request);
        assertNotEquals("t1",result.getCurrentTokenId()); verify(validationService).validate(s,"Chrome","127.0.0.1");
    }

    @Test void validateAndRotate_shouldRejectTokenReuse(){
        UserSession s=UserSession.builder().sessionId("s1").userId(1L).currentTokenId("actual").sessionVersion(1).expiresAt(Instant.now().plusSeconds(300)).build();
        when(repository.findById("s1")).thenReturn(Optional.of(s));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        assertThrows(RefreshTokenReuseException.class,()->service.validateAndRotate("s1","wrong",request));
        verify(repository).deleteById("s1");
    }
}
