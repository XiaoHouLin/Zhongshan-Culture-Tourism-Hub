package com.example.tourism_rear.config;

import com.example.tourism_rear.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireScheduler {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    public void closeExpiredOrders() {
        try {
            int closed = orderService.closeExpiredOrders();
            if (closed > 0) {
                log.info("Closed {} expired orders", closed);
            }
        } catch (Exception e) {
            log.warn("Failed to close expired orders", e);
        }
    }
}
