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
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.AddedChoreNotification;
import com.example.bartomiejjakubczak.thesis.models.Chore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateChoreFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private String assignedPersonKey;
    private CreateChoreFragment fragment;

    private TextView name;
    private TextView date;
    private TextView notes;
    private TextView assignedPerson;
    private Button saveButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChoresDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mFlatUsersDatabaseReference;

    private void sendNotifications() {
        final ArrayList<String> userIDs = new ArrayList<>();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String formattedDate = sdf.format(date);
        final AddedChoreNotification addedChoreNotification = new AddedChoreNotification("Chores", formattedDate, currentUserEmail);
        mFlatUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    userIDs.add(ds.getKey());
                }
                for (String key: userIDs) {
                    mUsersDatabaseReference.child(key).child("notifications").child(addedChoreNotification.getKey()).setValue(addedChoreNotification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean checkIfValidDate(String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            date = sdf.parse(value);
            if (date.before(new Date()) || !value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                date = sdf.parse(value);
                if (date.before(new Date()) || !value.equals(sdf.format(date))) {
                    date = null;
                }
            } catch (ParseException ex2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                    date = sdf.parse(value);
                    if (date.before(new Date()) || !value.equals(sdf.format(date))) {
                        date = null;
                    }
                } catch (ParseException ex3) {
                    ex3.printStackTrace();
                }
            }
        }
        return date != null;
    }

    private boolean checkIfEmpty(String string) {
        String testString = string.trim();
        return "".equals(testString);
    }

    private void saveInfoInDatabase(String name, String date, String notes, String assignedPerson) {

        boolean validName;
        boolean validDate;
        boolean assigned;

        if (checkIfEmpty(name.trim())) {
            this.name.setError("This field cannot be blank");
            validName = false;
        } else {
            validName = true;
        }

        if (!checkIfValidDate(date.trim())) {
            this.date.setError("The date is not valid");
            validDate = false;
        } else {
            validDate = true;
        }

        if (assignedPerson.equals("No assigned person")) {
            this.assignedPerson.setError("A person must be assigned");
            assigned = false;
        } else {
            assigned = true;
        }

        if (validName && validDate && assigned) {
            saveButton.setEnabled(false);
            Toast.makeText(getActivity(), "Chore has been created", Toast.LENGTH_SHORT).show();
            Chore newChore = new Chore(name.trim(), date.trim(), notes.trim(), currentUserEmail, assignedPersonKey);
            mChoresDatabaseReference.child(newChore.getKey()).setValue(newChore).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    saveButton.setEnabled(true);
                }
            });
            sendNotifications();
            this.name.setText("");
            this.date.setText("");
            this.notes.setText("");
            validName = false;
            validDate = false;
            assigned = false;
        }
    }

    private void setButtons() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfoInDatabase(name.getText().toString(), date.getText().toString(), notes.getText().toString(), assignedPerson.getText().toString());
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
        View view = inflater.inflate(R.layout.fragment_create_chore, container, false);
        name = view.findViewById(R.id.chore_name);
        date = view.findViewById(R.id.chore_dueDate);
        notes = view.findViewById(R.id.chore_notes);
        assignedPerson = view.findViewById(R.id.chore_assigned);
        try {
            String receivedAssignedKey = getArguments().getString("user_assigned_key");
            assignedPersonKey = receivedAssignedKey;
        } catch (NullPointerException e) {
            e.printStackTrace();
            assignedPersonKey = "";
        }
        try {
            String receivedAssignedTag = getArguments().getString("user_assigned_tag");
            assignedPerson.setText(receivedAssignedTag);
        } catch (NullPointerException e) {
            e.printStackTrace();
            assignedPerson.setText("No assigned person");
        }
        saveButton = view.findViewById(R.id.save_chore_button);
        setButtons();
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
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child("flatUsers")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
