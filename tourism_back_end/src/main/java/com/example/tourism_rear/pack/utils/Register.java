package com.example.tourism_rear.pack.utils;

import lombok.Data;


@Data
public class Register{
    private int uid;
    private String username;
    private String password;
    private Integer authority = 3; //默认权限，3为顾客
    private String email;
    private char csex;
    private String cname;
    private String cno;
    private int cage;
    private String caddress;
    private String cphone;
    private String cdate;
    private double balance = 0;
}
