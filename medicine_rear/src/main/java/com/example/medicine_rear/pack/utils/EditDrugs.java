package com.example.medicine_rear.pack.utils;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class EditDrugs extends DrugsInfo {
    private String oldMno;
}
