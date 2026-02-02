package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private Integer uid;
    private String oldPassword;
    private String newPassword;
}
