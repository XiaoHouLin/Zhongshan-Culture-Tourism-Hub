package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class Login {
    private String username;
    private String password;
    private String captchaToken;
    private String captchaInput;
}
