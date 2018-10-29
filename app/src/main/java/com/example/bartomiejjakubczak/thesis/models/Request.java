package com.example.bartomiejjakubczak.thesis.models;

import java.security.SecureRandom;

public class Request {

    private String topic;
    private String personInvolved;
    private String flatInvolved;
    private String key;

    public Request(String topic, String personInvolved, String flatInvolved) {
        this.topic = topic;
        this.personInvolved = personInvolved;
        this.flatInvolved = flatInvolved;
        key = generateKey();
    }

    public String getTopic() {
        return topic;
    }

    public String getPersonInvolved() {
        return personInvolved;
    }


    public String getKey() {
        return key;
    }

    public String getFlatInvolved() {
        return flatInvolved;
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
}
