package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    private String email;
    private String code;
    private String newPassword;
}
