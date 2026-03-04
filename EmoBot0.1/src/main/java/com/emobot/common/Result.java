package com.emobot.common;

import java.io.Serializable;

public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public void setCode(int code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }

    public static <T> Result<T> success(T data) {
        return new Result<T>(200, "success", data);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> success() {
        return (Result<T>) new Result<>(200, "success", null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> fail(int code, String message) {
        return (Result<T>) new Result<>(code, message, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> fail(String message) {
        return (Result<T>) new Result<>(500, message, null);
    }
}
