package com.example.bartomiejjakubczak.thesis.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChoreFragmentInfo extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private ChoreFragmentInfo fragment;
    private String choreKey;

    private TextView name;
    private TextView date;
    private TextView notes;
    private TextView personAssigned;
    private TextView personAssigning;
    private Button doneButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChoresDatabaseReference;

    private void loadChoreInfo() {
        mChoresDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText("Name: " + dataSnapshot.child("name").getValue().toString());
                date.setText("Due on: " + dataSnapshot.child("date").getValue().toString());
                notes.setText("Notes: " + dataSnapshot.child("notes").getValue().toString());
                personAssigned.setText("Assigned to: " + dataSnapshot.child("personAssigned").getValue().toString());
                personAssigning.setText("Assigned by: " + dataSnapshot.child("personAssigning").getValue().toString());
                if (personAssigned.getText().toString().equals(currentUserEmail)) {
                    doneButton.setVisibility(View.VISIBLE);
                }
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
        choreKey = getArguments().getString("chore_key");
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chore_info, container, false);
        name = view.findViewById(R.id.chore_name_info);
        date = view.findViewById(R.id.chore_date_info);
        notes = view.findViewById(R.id.chore_notes_info);
        personAssigned = view.findViewById(R.id.chore_assigned_info);
        personAssigning = view.findViewById(R.id.chore_assigning_info);
        doneButton = view.findViewById(R.id.complete_chore_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChoresDatabaseReference.removeValue();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
                getFragmentManager().popBackStack();
            }
        });
        loadChoreInfo();
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mChoresDatabaseReference = mFirebaseDatabase.getReference()
                .child("chores")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)))
                .child(choreKey);
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
