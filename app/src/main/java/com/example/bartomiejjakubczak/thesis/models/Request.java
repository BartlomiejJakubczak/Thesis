package com.example.bartomiejjakubczak.thesis.models;

public class Request {
    private String topic;
    private String message;
    private String sender;
    private String recipient;

    public Request(String topic, String message, String sender, String recipient) {
        this.topic = topic;
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
    }
}
