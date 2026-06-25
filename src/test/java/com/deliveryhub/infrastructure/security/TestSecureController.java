package com.deliveryhub.infrastructure.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecureController {
    @GetMapping("/api/v1/test-orders")
    public String getOrders() {
        return "orders";
    }

    @GetMapping("/api/v1/delivery/orders")
    public String getDeliveryOrders() {
        return "delivery orders";
    }

    @GetMapping("/api/v1/admin/dashboard")
    public String getAdminDashboard() {
        return "admin dashboard";
    }
}
