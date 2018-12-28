package com.example.bartomiejjakubczak.thesis.models;

import com.example.bartomiejjakubczak.thesis.interfaces.ListNotifications;

import java.security.SecureRandom;

public class AddedFoodShareNotification implements ListNotifications {

    private String key;
    private String topic;
    private String date;
    private String senderKey;

    public AddedFoodShareNotification(String topic, String date, String senderKey) {
        key = generateKey();
        this.topic = topic;
        this.date = date;
        this.senderKey = senderKey;
    }

    public AddedFoodShareNotification(String key, String topic, String date, String senderKey) {
        this.key = key;
        this.topic = topic;
        this.date = date;
        this.senderKey = senderKey;
    }

    private String generateKey() {
        final String characters = "0123456789ABCDEFGHIJKLMNPQRSTUWVXYZabcdefghijklmnoprqstuvwxyz";
        final int length = 20;
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }

    @Override
    public int getListNotificationType() {
        return ListNotifications.TYPE_FOODSHARE_ADDED_NOTIFICATION;
    }

    public String getKey() {
        return key;
    }

    public String getTopic() {
        return topic;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public String getDate() {
        return date;
    }
}
