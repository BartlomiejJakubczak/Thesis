package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.ManageFlatFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManageFlatFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private final static String TAG = "ManageFlatFragment";
    private final String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private String oldName;
    private String oldAddress;
    private Boolean isOldName = true;
    private Boolean isOldAddress = true;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedFlatDatabaseReference;
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private RecyclerView recyclerView;
    private ManageFlatFragmentAdapter manageFlatFragmentAdapter;
    private EditText flatName;
    private ImageButton editFlatNameButton;
    private EditText flatAddress;
    private TextView flatCode;
    private ImageButton editFlatAddressButton;
    private Button saveChangesButton;


    private void setButtons() {
        if (loadStringFromSharedPrefs(getActivity(), "shared_prefs_is_owner").equals("yes")) {

            editFlatNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flatName.isEnabled()) {
                        flatName.setEnabled(false);
                    } else {
                        flatName.setEnabled(true);
                    }
                }
            });

            editFlatAddressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flatAddress.isEnabled()) {
                        flatAddress.setEnabled(false);
                    } else {
                        flatAddress.setEnabled(true);
                    }
                }
            });

            saveChangesButton.setEnabled(false);
            saveChangesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveChangesButton.setEnabled(false);
                    final String newName = flatName.getText().toString();
                    final String newAddress = flatAddress.getText().toString();

                    if (checkIfEmpty(newName)) {
                        flatName.setError("This field cannot be blank");
                    } else if (checkIfSpecialCharacter(newName)) {
                        flatName.setError("This field cannot contain special characters");
                    } else if (!newName.equals(oldName)){
                        mSearchedFlatDatabaseReference.child("name").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Changes saved", Toast.LENGTH_SHORT).show();
                                oldName = newName;
                            }
                        });
                    }

                    if (checkIfEmpty(newAddress)) {
                        flatAddress.setError("This field cannot be blank");
                    } else if (checkIfSpecialCharacter(newAddress)) {
                        flatAddress.setError("This field cannot contain special characters");
                    } else if(!newAddress.equals(oldAddress)){
                        mSearchedFlatDatabaseReference.child("address").setValue(newAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Changes saved", Toast.LENGTH_SHORT).show();
                                oldAddress = newAddress;
                            }
                        });
                    }
                }
            });

        } else {
            editFlatNameButton.setVisibility(View.GONE);
            editFlatAddressButton.setVisibility(View.GONE);
            saveChangesButton.setVisibility(View.GONE);
        }
    }

    private boolean checkIfSpecialCharacter(String string) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        return m.find();
    }

    private boolean checkIfEmpty(String string) {
        String testString = string.trim();
        return "".equals(testString);
    }

    private void setEditTexts() {
        mSearchedFlatDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                oldName = dataSnapshot.child("name").getValue().toString();
                flatName.setText(oldName);
                flatName.setEnabled(false);
                oldAddress = dataSnapshot.child("address").getValue().toString();
                flatAddress.setText(oldAddress);
                flatAddress.setEnabled(false);
                flatCode.setText("Search code: " + dataSnapshot.child("searchCode").getValue().toString());
                flatName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (oldName.equals(s.toString())) {
                            Log.d(TAG, isOldName.toString());
                            Log.d(TAG, isOldAddress.toString());
                            isOldName = true;
                            if (isOldAddress) {
                                saveChangesButton.setEnabled(false);
                            }
                        } else {
                            isOldName = false;
                            saveChangesButton.setEnabled(true);
                        }
                    }
                });
                flatAddress.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (oldAddress.equals(s.toString())) {
                            Log.d(TAG, isOldName.toString());
                            Log.d(TAG, isOldAddress.toString());
                            isOldAddress = true;
                            if (isOldName) {
                                saveChangesButton.setEnabled(false);
                            }
                        } else {
                            isOldAddress = false;
                            saveChangesButton.setEnabled(true);
                        }
                    }
                });
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

        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        flatName = view.findViewById(R.id.flat_name_fragment);
        flatAddress = view.findViewById(R.id.flat_address_fragment);
        flatCode = view.findViewById(R.id.flatCode_textView);

        editFlatNameButton = view.findViewById(R.id.edit_flat_name_button);
        editFlatAddressButton = view.findViewById(R.id.edit_flat_address_button);
        saveChangesButton = view.findViewById(R.id.save_changes_button);

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
