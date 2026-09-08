package com.gayacademy.common.config;

import com.gayacademy.chat.service.ChatService;
import com.gayacademy.chat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

/** Detecta conexão e queda do WebSocket para manter estado de presença (CHT-04). */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final ChatService chatService;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        UUID userId = UUID.fromString(principal.getName());
        presenceService.markOnline(userId);
        chatService.entregarMensagensPendentes(userId);
        log.debug("WebSocket conectado: {}", userId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        UUID userId = UUID.fromString(principal.getName());
        presenceService.markOffline(userId);
        log.debug("WebSocket desconectado: {}", userId);
    }
}
