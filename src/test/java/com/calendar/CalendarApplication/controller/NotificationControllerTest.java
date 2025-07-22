package com.calendar.CalendarApplication.controller;

import com.calendar.CalendarApplication.controller.NotificationController;
import com.calendar.CalendarApplication.dtos.notification.NotificationResponseDto;
import com.calendar.CalendarApplication.dtos.notification.NotificationToUpdateDto;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.repository.NotificationRepository;
import com.calendar.CalendarApplication.services.NotificationService;
import com.calendar.CalendarApplication.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationController notificationController;

    private final String validBearer = "Bearer valid-token";
    private final String invalidBearer = "Bearer invalid-token";

    private NotificationToUpdateDto updateDto;

    @BeforeEach
    void setup() {
        updateDto = new NotificationToUpdateDto(1, "New Title", "New Description", true);
    }

    @Test
    @DisplayName("should update notification successfully with valid token")
    void updateNotification_success() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        var updatedNotification = new com.calendar.CalendarApplication.entity.Notification();
        updatedNotification.setTitle(updateDto.title());
        updatedNotification.setDescription(updateDto.description());
        updatedNotification.setHasSeen(updateDto.hasSeen());

        when(notificationRepository.findUserByNotificationId(updateDto.id())).thenReturn(user);
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(notificationService.updateNotification(updateDto)).thenReturn(updatedNotification);

        ResponseEntity<?> response = notificationController.updateNotification(updateDto, validBearer);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Notificação atualizada com sucesso!", body.get("Message"));
        assertNotNull(body.get("notificação:"));

        verify(notificationService).updateNotification(updateDto);
    }

    @Test
    @DisplayName("should return 403 if authorization header missing or malformed")
    void updateNotification_missingBearer() {
        ResponseEntity<?> response = notificationController.updateNotification(updateDto, "InvalidHeader");
        assertEquals(403, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Bearer token errado ou nao definido", body.get("Erro"));
    }

    @Test
    @DisplayName("should return 401 if token validation fails")
    void updateNotification_invalidToken() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        when(notificationRepository.findUserByNotificationId(updateDto.id())).thenReturn(user);
        when(jwtUtil.validateToken("invalid-token", user.getUsername())).thenReturn(false);

        ResponseEntity<?> response = notificationController.updateNotification(updateDto, invalidBearer);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Token inválido ou expirado", response.getBody());
    }

    @Test
    @DisplayName("should return 404 if notification not found")
    void updateNotification_notificationNotFound() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        when(notificationRepository.findUserByNotificationId(updateDto.id())).thenReturn(user);
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(notificationService.updateNotification(updateDto)).thenReturn(null);

        ResponseEntity<?> response = notificationController.updateNotification(updateDto, validBearer);

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Notificação não encontrada", body.get("Message"));
    }

    @Test
    @DisplayName("should return 500 if service throws exception")
    void updateNotification_internalError() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        when(notificationRepository.findUserByNotificationId(updateDto.id())).thenReturn(user);
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(notificationService.updateNotification(updateDto)).thenThrow(new RuntimeException("Erro inesperado"));

        ResponseEntity<?> response = notificationController.updateNotification(updateDto, validBearer);

        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Ocorreu um erro interno ao processar a solicitação. Tente novamente mais tarde.", body.get("message"));
        assertTrue(body.get("errorDetail").toString().contains("Erro inesperado"));
    }
}
