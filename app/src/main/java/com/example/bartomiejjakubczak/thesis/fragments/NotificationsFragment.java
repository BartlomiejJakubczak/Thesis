package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.adapters.RequestsFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.ListNotifications;
import com.example.bartomiejjakubczak.thesis.models.RequestJoinNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment implements FirebaseConnection {

    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private FirebaseDatabase mFirebaseDatabase;
    public final List<ListNotifications> receivedNotifications = new ArrayList<ListNotifications>();
    public final List<ListNotifications> receivedNotificationsCopy = new ArrayList<ListNotifications>();

    private RecyclerView recyclerView;
    private DatabaseReference mReceivedNotificationsDatabaseReference;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private View setViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = view.findViewById(R.id.fragment_notifications_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        loadNotifications();
        return view;
    }

    private void loadNotifications() {
        mReceivedNotificationsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    switch (ds.child("topic").getValue().toString()) {
                        case "Join flat":
                            if (ds.child("personInvolvedKey").getValue().toString().equals(userDotlessEmail)) {
                                final RequestJoinNotification requestJoinNotification = new RequestJoinNotification(
                                        ds.child("topic").getValue().toString(),
                                        ds.child("personInvolvedKey").getValue().toString(),
                                        ds.child("personInvolvedTag").getValue().toString(),
                                        ds.child("flatInvolvedKey").getValue().toString(),
                                        ds.child("flatInvolvedName").getValue().toString(),
                                        ds.child("key").getValue().toString(),
                                        ds.child("sentNotificationKey").getValue().toString()
                                );
                                receivedNotifications.add(requestJoinNotification);
                            }
                    }
                }
                receivedNotificationsCopy.addAll(receivedNotifications);
                RequestsFragmentAdapter requestsFragmentAdapter = new RequestsFragmentAdapter(MainActivity.getContext(), receivedNotifications, receivedNotificationsCopy);
                recyclerView.setAdapter(requestsFragmentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return setViews(inflater, container);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(userDotlessEmail);
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mReceivedNotificationsDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("receivedNotifications");
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child("flats");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }
}
