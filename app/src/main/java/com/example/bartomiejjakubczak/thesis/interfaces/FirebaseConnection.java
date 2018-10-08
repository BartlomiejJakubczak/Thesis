package com.example.bartomiejjakubczak.thesis.interfaces;

public interface FirebaseConnection {
    void initializeFirebaseComponents();
    void initializeFirebaseDatabaseReferences(String dotlessEmail);
}
