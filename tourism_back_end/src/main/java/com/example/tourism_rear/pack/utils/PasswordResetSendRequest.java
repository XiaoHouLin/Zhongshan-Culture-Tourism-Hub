package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class PasswordResetSendRequest {
    private String username;
    private String email;
}
