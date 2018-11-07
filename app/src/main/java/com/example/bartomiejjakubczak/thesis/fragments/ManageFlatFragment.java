package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.ManageFlatFragmentAdapter;
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

public class ManageFlatFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private final static String TAG = "ManageFlatFragment";
    private final String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private String oldName;
    private String oldAddress;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedFlatDatabaseReference;
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private RecyclerView recyclerView;
    private ManageFlatFragmentAdapter manageFlatFragmentAdapter;
    private TextView recyclerViewLabel;
    private EditText flatName;
    private ImageButton editFlatNameButton;
    private EditText flatAddress;
    private ImageButton editFlatAddressButton;
    private Button saveChangesButton;
    private Button deleteFlatButton;


    private void setButtons() {
        editFlatNameButton.setEnabled(false);
        editFlatAddressButton.setEnabled(false);
        saveChangesButton.setEnabled(false);

        editFlatNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        editFlatAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        deleteFlatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setEditTexts() {
        mSearchedFlatDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                oldName = dataSnapshot.child("name").getValue().toString();
                flatName.setText(oldName);
                oldAddress = dataSnapshot.child("address").getValue().toString();
                flatAddress.setText(oldAddress);
                editFlatNameButton.setEnabled(true);
                editFlatAddressButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setRecyclerView() {
        final ArrayList<User> users = new ArrayList<>();
        final ArrayList<String> usersInFlatKeys = new ArrayList<>();
        mFlatUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    usersInFlatKeys.add(ds.getKey());
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
                    if (usersInFlatKeys.contains(ds.getKey())) {
                        users.add(new User(
                                ds.child("name").getValue().toString(),
                                ds.child("surname").getValue().toString(),
                                ds.child("email").getValue().toString(),
                                ds.child("tag").getValue().toString()
                        ));
                    }
                }
                manageFlatFragmentAdapter = new ManageFlatFragmentAdapter(getActivity(), users);
                recyclerView.setAdapter(manageFlatFragmentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_flat, container, false);

        recyclerViewLabel = view.findViewById(R.id.users_recyclerView_label);
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        flatName = view.findViewById(R.id.flat_name_fragment);
        flatAddress = view.findViewById(R.id.flat_address_fragment);

        editFlatNameButton = view.findViewById(R.id.edit_flat_name_button);
        editFlatAddressButton = view.findViewById(R.id.edit_flat_address_button);
        saveChangesButton = view.findViewById(R.id.save_changes_button);
        deleteFlatButton = view.findViewById(R.id.delete_flat_button);
        setButtons();
        setEditTexts();
        setRecyclerView();
        return view;
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
        mSearchedFlatDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats))
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference().child("flatUsers")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
