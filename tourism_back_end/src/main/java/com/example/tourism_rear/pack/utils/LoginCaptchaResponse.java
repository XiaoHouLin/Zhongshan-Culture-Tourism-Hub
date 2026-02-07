package com.example.tourism_rear.pack.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginCaptchaResponse {
    private String token;
    private String question;
    private long expiresIn;
}
