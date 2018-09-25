package com.example.bartomiejjakubczak.thesis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class LogInActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeFirebaseComponents();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // The user is signed in
                    if (!user.isEmailVerified()) {
                        verifyEmail(user);
                        finish();
                    } else {
                        createNewUserInDatabase(user);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // The user is not signed in
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
    }

    private void verifyEmail(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, getString(R.string.logs_email_sent));
                } else {
                    Log.d(TAG, getString(R.string.logs_email_fail));
                }
            }
        });
        Intent intent = new Intent(this, VerificationActivity.class);
        startActivity(intent);
    }

    private void createNewUserInDatabase(final FirebaseUser user) {
        final String userEmail = user.getEmail();
        String userDisplayName = user.getDisplayName();
        String[] userNameAndSurname = userDisplayName.split(" ");
        final String userName = userNameAndSurname[0];
        final String userSurname = userNameAndSurname[1];

        mUsersDatabaseReference.orderByChild(getString(R.string.firebase_reference_email))
                .equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    String dotlessEmail = userEmail.replaceAll("[\\s.]", "");
                    mUsersDatabaseReference.child(dotlessEmail).setValue(new User(userName, userSurname, userEmail));
                    Log.d(TAG, getString(R.string.logs_user_created));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, " " + databaseError.getDetails() + "  " + databaseError.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                //TODO duplicaion of activities after signup
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(LogInActivity.this, getString(R.string.logs_signin_canceled), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
