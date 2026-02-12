package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.utils.RealtimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class RealtimeService {
    private final Map<Integer, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter register(Integer uid, boolean isAdmin) {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> removeEmitter(uid, isAdmin, emitter));
        emitter.onTimeout(() -> removeEmitter(uid, isAdmin, emitter));
        emitter.onError(e -> removeEmitter(uid, isAdmin, emitter));

        if (isAdmin) {
            adminEmitters.add(emitter);
        } else if (uid != null) {
            userEmitters.computeIfAbsent(uid, key -> new CopyOnWriteArrayList<>()).add(emitter);
        }

        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("ok"));
        } catch (IOException e) {
            removeEmitter(uid, isAdmin, emitter);
        }
        return emitter;
    }

    public void broadcastOrderUpdate(Integer uid, Integer orderId, Integer orderStatus) {
        RealtimeEvent event = new RealtimeEvent(
                "ORDER_UPDATED",
                uid,
                orderId,
                orderStatus,
                null,
                System.currentTimeMillis()
        );
        sendToUser(uid, event);
        sendToAdmins(event);
    }

    public void broadcastGlobalRefresh() {
        RealtimeEvent event = new RealtimeEvent(
                "GLOBAL_REFRESH",
                null,
                null,
                null,
                null,
                System.currentTimeMillis()
        );
        sendToAllUsers(event);
        sendToAdmins(event);
    }

    public void notifyAdminPendingOrder(Integer uid, Integer orderId) {
        RealtimeEvent event = new RealtimeEvent(
                "ADMIN_PENDING_ORDER",
                uid,
                orderId,
                1,
                "您收到一个待处理的订单",
                System.currentTimeMillis()
        );
        sendToAdmins(event);
    }

    public void notifyAdminRefundRequest(Integer uid, Integer orderId) {
        RealtimeEvent event = new RealtimeEvent(
                "ADMIN_REFUND_REQUEST",
                uid,
                orderId,
                5,
                "您收到一个退款申请",
                System.currentTimeMillis()
        );
        sendToAdmins(event);
    }

    public boolean isUserOnline(Integer uid) {
        if (uid == null) {
            return false;
        }
        List<SseEmitter> emitters = userEmitters.get(uid);
        return emitters != null && !emitters.isEmpty();
    }

    public void sendToUser(Integer uid, RealtimeEvent event) {
        if (uid == null) {
            return;
        }
        List<SseEmitter> emitters = userEmitters.get(uid);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        emitters.forEach(emitter -> sendSafe(uid, false, emitter, event));
    }

    public void sendToAdmins(RealtimeEvent event) {
        if (adminEmitters.isEmpty()) {
            return;
        }
        adminEmitters.forEach(emitter -> sendSafe(null, true, emitter, event));
    }

    private void sendToAllUsers(RealtimeEvent event) {
        if (userEmitters.isEmpty()) {
            return;
        }
        userEmitters.forEach((uid, emitters) -> {
            if (emitters == null || emitters.isEmpty()) {
                return;
            }
            emitters.forEach(emitter -> sendSafe(uid, false, emitter, event));
        });
    }

    private void sendSafe(Integer uid, boolean isAdmin, SseEmitter emitter, RealtimeEvent event) {
        try {
            emitter.send(SseEmitter.event().name(event.getType()).data(event));
        } catch (IOException e) {
            removeEmitter(uid, isAdmin, emitter);
        }
    }

    private void removeEmitter(Integer uid, boolean isAdmin, SseEmitter emitter) {
        if (isAdmin) {
            adminEmitters.remove(emitter);
            return;
        }
        if (uid == null) {
            return;
        }
        List<SseEmitter> emitters = userEmitters.get(uid);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(uid);
            }
        }
    }
}
