package com.example.bartomiejjakubczak.thesis.models;

import java.util.ArrayList;
import java.util.List;

public class RoomUsers {
    private List<String> emailsOfUsersInRoom;

    public RoomUsers(String ownerEmail) {
        emailsOfUsersInRoom = new ArrayList<>();
        emailsOfUsersInRoom.add(ownerEmail);
    }
}
