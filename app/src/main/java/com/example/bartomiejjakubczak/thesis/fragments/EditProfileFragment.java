package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditProfileFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private static final String TAG = "EditProfileFragment";
    private final String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private String oldTag;
    private List<String> parameters = new ArrayList<>();
    private boolean validTagDuplicate = true;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedUserDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mSearchedUserFlatsDatabaseReference;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mReceivedNotificationsDatabaseReference;

    private EditText tag;
    private Button doneButton;

    private View setViews(LayoutInflater inflater, ViewGroup container) {
        final View view = inflater.inflate(R.layout.activity_edit_profile, container, false);
        tag = view.findViewById(R.id.edit_flat_name);
        setEditTexts();
        doneButton = view.findViewById(R.id.edit_profile_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String newTag = tag.getText().toString();
                checkIfCorrectTag(newTag);
            }
        });
        return view;
    }

    private void setEditTexts() {
        mSearchedUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    parameters.add(ds.getValue().toString());
                }
                tag.setText(parameters.get(3));
                oldTag = parameters.get(3);
                Log.d(TAG, oldTag);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkIfCorrectTag(String newTag) {
        if (checkIfEmpty(newTag)) {
            tag.setError(getString(R.string.error_blank_field));
            tag.setText(oldTag);
            tag.setHintTextColor(getResources().getColor(R.color.red));
        } else {
            checkIfDuplicate(newTag);
        }
    }

    private boolean checkIfEmpty(String newTag) {
        String testString = newTag.trim();
        return "".equals(testString);
    }

    private void checkIfDuplicate(final String newTag) {
        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("tag").getValue().toString().equals(newTag)) {
                        validTagDuplicate = false;
                        tag.setError(getString(R.string.error_tag_exists));
                        tag.setText(oldTag);
                        tag.setHintTextColor(getResources().getColor(R.color.red));
                    }
                }
                if (validTagDuplicate) {
                    doneButton.setEnabled(false);
                    putStringToSharedPrefs(MainActivity.getContext(), "shared_prefs_user_tag", newTag);
                    mUserDatabaseReference.child(currentUserEmail).child("tag").setValue(newTag);
                    mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                if (ds.child("ownerTag").getValue().toString().equals(oldTag)) {
                                    mFlatsDatabaseReference.child(ds.getKey()).child("ownerTag").setValue(newTag);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    mReceivedNotificationsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                if (ds.child("personInvolvedTag").getValue().toString().equals(oldTag)) {
                                    mReceivedNotificationsDatabaseReference.child(ds.getKey()).child("personInvolvedTag").setValue(newTag);
                                    doneButton.setEnabled(true);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(mFirebaseAuth.getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return setViews(inflater, container);
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mSearchedUserDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users)).child(dotlessEmail);
        mUserDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mSearchedUserFlatsDatabaseReference = mFirebaseDatabase.getReference().child("userFlats").child(dotlessEmail);
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child("flats");
        mReceivedNotificationsDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("receivedNotifications");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return null;
    }
}
