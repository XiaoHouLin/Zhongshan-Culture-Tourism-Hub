package com.example.medicine_rear.pack.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private int code;
    private String message;
    private Object data;

    // 成功响应的静态方法
    public static  Result success(Object data) {
        return new Result(1, "success", data);
    }
    // 成功响应的静态方法,不需要返回数据
    public static  Result success() {
        return new Result(1, "success", null);
    }
    // 失败响应的静态方法
    public static  Result error(String message) {
        return new Result(0, message, null);
    }
}

