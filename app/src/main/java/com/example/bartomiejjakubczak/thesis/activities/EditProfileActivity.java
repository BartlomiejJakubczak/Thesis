package com.example.bartomiejjakubczak.thesis.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity implements FirebaseConnection {

    private static final String TAG = "EditProfileActivity";
    private String oldTag;
    private List<String> parameters = new ArrayList<>();
    private boolean validTagEmpty = false;
    private boolean validTagDuplicate = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedUserDatabaseReference;
    private DatabaseReference mUserDatabaseReference;

    private EditText tag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(mFirebaseAuth.getCurrentUser().getEmail().replaceAll("[\\s.]", ""));
        setEditTexts();
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
    }

    private void setViews() {
        tag = findViewById(R.id.edit_flat_name);
        Button doneButton = findViewById(R.id.edit_profile_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTag = tag.getText().toString();
                checkIfCorrectTag(newTag);
            }
        });
    }

    private void setEditTexts() {
        mSearchedUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    parameters.add(ds.getValue().toString());
                }
                setContentView(R.layout.activity_edit_profile);
                setViews();
                tag.setText(parameters.get(3));
                oldTag = parameters.get(3);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkIfCorrectTag(String newTag) {
        if (checkIfEmpty(newTag)) {
            validTagEmpty = false;
            verdictTag(newTag);
        } else {
            validTagEmpty = true;
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
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child(getString(R.string.user_node_tag)).getValue().toString().equals(newTag)) {
                        validTagDuplicate = false;
                    } else {
                        validTagDuplicate = true;
                    }
                    verdictTag(newTag);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void verdictTag(String newTag) {
        if (!validTagEmpty) {
            tag.setError(getString(R.string.error_blank_field));
            tag.setText(oldTag);
            tag.setHintTextColor(getResources().getColor(R.color.red));
            tag.requestFocus();
        } else {
            if (!validTagDuplicate) {
                tag.setError(getString(R.string.error_tag_exists));
                tag.setText(oldTag);
                tag.setHintTextColor(getResources().getColor(R.color.red));
                tag.requestFocus();
            } else {
                updateTag(newTag);
            }
        }
    }

    private void updateTag(String newTag) {
        mSearchedUserDatabaseReference.child(getString(R.string.user_node_tag)).setValue(newTag.trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //TODO toast maybe? think of a way to inform user about completion
                finish();
            }
        });
    }
}
