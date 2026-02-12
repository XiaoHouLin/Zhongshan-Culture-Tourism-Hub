package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class EmailChangeCodeRequest {
    private Integer uid;
    private String email;
}
