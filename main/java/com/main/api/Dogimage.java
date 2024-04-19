package com.main.api;

public class Dogimage {
    private String message;
    private String status;

    public Dogimage(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public String getStatus() {
        return status;
    }
}
