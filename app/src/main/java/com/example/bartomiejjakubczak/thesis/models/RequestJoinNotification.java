package com.example.bartomiejjakubczak.thesis.models;

import com.example.bartomiejjakubczak.thesis.interfaces.ListNotifications;

import java.security.SecureRandom;

public class RequestJoinNotification implements ListNotifications {

    private String topic;
    private String personInvolvedKey;
    private String personInvolvedTag;
    private String flatInvolvedKey;
    private String flatInvolvedName;
    private String key;

    public RequestJoinNotification(String topic, String personInvolvedKey, String personInvolvedTag, String flatInvolvedKey, String flatInvolvedName) {
        this.topic = topic;
        this.personInvolvedKey = personInvolvedKey;
        this.personInvolvedTag = personInvolvedTag;
        this.flatInvolvedKey = flatInvolvedKey;
        this.flatInvolvedName = flatInvolvedName;
        key = generateKey();
    }

    public RequestJoinNotification(String topic, String personInvolvedKey, String personInvolvedTag, String flatInvolvedKey, String flatInvolvedName, String key) {
        this.topic = topic;
        this.personInvolvedKey = personInvolvedKey;
        this.personInvolvedTag = personInvolvedTag;
        this.flatInvolvedKey = flatInvolvedKey;
        this.flatInvolvedName = flatInvolvedName;
        this.key = key;
    }

    public RequestJoinNotification(String topic, String personInvolvedKey, String flatInvolvedKey) {
        this.key = generateKey();
        this.topic = topic;
        this.flatInvolvedKey = flatInvolvedKey;
        this.personInvolvedKey = personInvolvedKey;
    }

    public String getTopic() {
        return topic;
    }

    public String getPersonInvolvedKey() {
        return personInvolvedKey;
    }

    public String getKey() {
        return key;
    }

    public String getFlatInvolvedKey() {
        return flatInvolvedKey;
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
        return ListNotifications.TYPE_JOIN_NOTIFICATION;
    }

    public String getPersonInvolvedTag() {
        return personInvolvedTag;
    }

    public void setPersonInvolvedTag(String personInvolvedTag) {
        this.personInvolvedTag = personInvolvedTag;
    }

    public String getFlatInvolvedName() {
        return flatInvolvedName;
    }

    public void setFlatInvolvedName(String flatInvolvedName) {
        this.flatInvolvedName = flatInvolvedName;
    }
}
