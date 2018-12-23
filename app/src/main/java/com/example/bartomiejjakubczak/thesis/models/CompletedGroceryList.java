package com.example.bartomiejjakubczak.thesis.models;

import java.security.SecureRandom;

/**
 * Created by Bartłomiej Jakubczak on 21.12.2018.
 */

public class CompletedGroceryList {
    private String key;
    private String completionDate;
    private String buyerID;
    private String receiptURI;

    public CompletedGroceryList(String key, String completionDate, String buyerID, String receiptURI) {
        this.key = key;
        this.completionDate = completionDate;
        this.buyerID = buyerID;
        this.receiptURI = receiptURI;
    }

    public CompletedGroceryList(String completionDate, String buyerID, String receiptURI) {
        this.key = generateKey(20);
        this.completionDate = completionDate;
        this.buyerID = buyerID;
        this.receiptURI = receiptURI;
    }

    private String generateKey(int length) {
        final String characters = "0123456789ABCDEFGHIJKLMNPQRSTUWVXYZabcdefghijklmnoprqstuvwxyz";
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }

    public String getKey() {
        return key;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public String getBuyerID() {
        return buyerID;
    }

    public String getReceiptURI() {
        return receiptURI;
    }

    public void setReceiptURI(String receiptURI) {
        this.receiptURI = receiptURI;
    }
}
