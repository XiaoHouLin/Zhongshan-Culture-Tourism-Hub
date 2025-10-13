package com.example.medicine_rear.pack.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class EditUser extends UserInfo {
    private String oldUid;
}
