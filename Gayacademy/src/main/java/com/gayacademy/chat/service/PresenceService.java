package com.gayacademy.chat.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Estado de presença em memória — não sobrevive a restart (instância única). */
@Service
public class PresenceService {

    private final Set<UUID> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void markOnline(UUID userId) {
        onlineUsers.add(userId);
    }

    public void markOffline(UUID userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(UUID userId) {
        return onlineUsers.contains(userId);
    }
}
