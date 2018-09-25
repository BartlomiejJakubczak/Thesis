package com.example.bartomiejjakubczak.thesis;

//TODO LOG: 05.09.18 database rules are changed to test mode and the app crashes without Internet
//TODO LOG CONTINUED: I successfully search for user and check his hasRoom value
//TODO LOG CONTINUED: Think of a way to present user with option to create a room if hasRoom is false
//TODO LOG 13.09.18: I think I will use 4 top-level nodes method
//TODO LOG CONTINUED: context of mainactivity has to be passed to alertdialog
//TODO LOG 21.09.18: think of some way to wait for update of hasRoom (or new technique as a whole)
//TODO LOG CONTINUED: next steps: show created room in a way you think of (in side panel or something)
//TODO LOG CONTINUED: next steps continued: get rid of hasRoom by searching for rooms in UserRooms if given user has any, then do activity transition in OnCompleteListener of CreateRoom in CreateRoomActivity
//TODO LOG 25.09.18: Replace word room with another word
//TODO LOG CONTINUED: Make suitable interfaces to cleanup your code
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.dialogs.CreateRoomDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String loggedUserEmail;
    private DialogFragment createRoomAlertDialog;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mSearchedUserReference;
    private DatabaseReference mUsersRoomsDatabaseReference;
    private DatabaseReference mSearchedUserRoomsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeFirebaseComponents();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setContentView(R.layout.activity_main);
                    loggedUserEmail = user.getEmail().replaceAll("[\\s.]", "");
                    initializeFirebaseDatabaseReferences(loggedUserEmail);
                    decideToShowCreateRoomDialog();
                } else {
                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void setViews() {

    }

    private void showCreateRoomAlertDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        createRoomAlertDialog = new CreateRoomDialogFragment();
        createRoomAlertDialog.show(fragmentManager, getString(R.string.tags_create_room_dialog));
    }

    private void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mSearchedUserReference = mUsersDatabaseReference.child(dotlessEmail);
        mUsersRoomsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_rooms));
        mSearchedUserRoomsReference = mUsersRoomsDatabaseReference.child(dotlessEmail);
    }

    private void decideToShowCreateRoomDialog() {
        mSearchedUserRoomsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showCreateRoomAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
