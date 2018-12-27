package com.example.bartomiejjakubczak.thesis.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.AssignFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AssignPersonFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private AssignPersonFragment fragment;

    private RecyclerView recyclerView;
    private AssignFragmentAdapter adapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private void loadUsers() {
        final ArrayList<String> userKeys = new ArrayList<>();
        final ArrayList<User> usersInFlat = new ArrayList<>();
        mFlatUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    userKeys.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (userKeys.contains(ds.getKey())) {
                        usersInFlat.add(new User(
                                ds.child("name").getValue().toString(),
                                ds.child("surname").getValue().toString(),
                                ds.child("email").getValue().toString(),
                                ds.child("tag").getValue().toString()));
                    }
                }
                adapter = new AssignFragmentAdapter(getActivity(), fragment, usersInFlat);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = this;
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assign, container, false);
        recyclerView = view.findViewById(R.id.chore_assign_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        loadUsers();
        return view;
    }


    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child("flatUsers")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child("users");
    }



    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
