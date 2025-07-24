package com.calendar.CalendarApplication.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    DotenvUtil dotenvUtil;

    JwtUtil jwtUtil;

    private final String secretKey = "01234567890123456789012345678901";

    @BeforeEach
    void setup() {
        when(dotenvUtil.getValue("JWT_SECRET_KEY")).thenReturn(secretKey);
        jwtUtil = new JwtUtil(dotenvUtil);
        jwtUtil.init();
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, username));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void shouldInvalidateTokenForWrongUsername() {
        String token = jwtUtil.generateToken("testuser");

        assertFalse(jwtUtil.validateToken(token, "otheruser"));
    }

    @Test
    void shouldInvalidateExpiredToken() {

        JwtUtil spyJwtUtil = Mockito.spy(jwtUtil);
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() - 1000));
        doReturn(claims).when(spyJwtUtil).extractClaims(anyString());

        assertFalse(spyJwtUtil.validateToken("anytoken", "testuser"));
    }
}