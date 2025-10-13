package com.example.medicine_rear.pack.utils;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfo {
    private Integer uid;
    private String username;
    private String password ="123456";
    private Integer authority;
    private String email;
    private String cno;
    private String cname;
    private char csex;
    private int cage;
    private String caddress;
    private String cphone;
    private String csymptom;
    private Date cdate;
    private String cremark;
    private Double balance;
}
