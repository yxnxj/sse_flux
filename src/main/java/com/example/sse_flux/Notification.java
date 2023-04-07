package com.example.sse_flux;

public class Notification {

    private final Integer id;
    private final String message;

    public Notification(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }
}