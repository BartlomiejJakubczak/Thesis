package com.example.bartomiejjakubczak.thesis.interfaces;

public interface ListNotifications {
    int TYPE_JOIN_NOTIFICATION = 1;
    int TYPE_FOODSHARE_ADDED_NOTIFICATION = 2;
    int TYPE_GROCERY_ADDED_NOTIFICATION = 3;
    int getListNotificationType();
}
