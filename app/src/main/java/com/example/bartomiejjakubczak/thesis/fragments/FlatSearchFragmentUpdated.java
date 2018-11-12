package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.RequestJoinNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FlatSearchFragmentUpdated  extends Fragment implements FirebaseConnection, SharedPrefs {

    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    private EditText codeInput;
    private Button submitButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mRequestSenderDatabaseReference;
    private DatabaseReference mRequestReceiverDatabaseReference;

    private void checkCode(final String inputCode) {
        submitButton.setEnabled(false);

        final boolean[] isFound = {false};
        final String[] flatKey = new String[1];
        final String[] flatOwnerKey = new String[1];
        final String[] flatName = new String[1];
        final String senderTag = loadStringFromSharedPrefs(getActivity(), "shared_prefs_user_tag");
        final String[] sentNotificationKey = new String[1];

        mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (inputCode.equals(ds.child("searchCode").getValue())) {
                        isFound[0] = true;
                        flatKey[0] = ds.child("key").getValue().toString();
                        flatName[0] = ds.child("name").getValue().toString();
                        flatOwnerKey[0] = ds.child("owner").getValue().toString();
                        RequestJoinNotification requestJoinNotification = new RequestJoinNotification("Join flat", userDotlessEmail, flatKey[0]);
                        sentNotificationKey[0] = requestJoinNotification.getKey();
                        mRequestSenderDatabaseReference.child(requestJoinNotification.getKey()).setValue(requestJoinNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                RequestJoinNotification requestJoinNotification = new RequestJoinNotification("Join flat", userDotlessEmail, flatOwnerKey[0], senderTag, flatKey[0], flatName[0], sentNotificationKey[0]);
                                mRequestReceiverDatabaseReference.child("receivedNotifications").child(requestJoinNotification.getKey()).setValue(requestJoinNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getActivity(), "The notification has been sent", Toast.LENGTH_SHORT).show();
                                        submitButton.setEnabled(true);
                                    }
                                });
                            }
                        });
                    }
                }
                if (!isFound[0]) {
                    codeInput.setError("The code is invalid");
                    submitButton.setEnabled(true);
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
        initializeFirebaseDatabaseReferences(userDotlessEmail);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_flats_withcode, container, false);
        codeInput = view.findViewById(R.id.flat_search_code_edittext);
        submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = codeInput.getText().toString();
                checkCode(inputCode);
            }
        });
        return view;
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child("flats");
        mRequestSenderDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("sentNotifications");
        mRequestReceiverDatabaseReference = mFirebaseDatabase.getReference().child("notifications");
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, "No flat yet");
    }
}
