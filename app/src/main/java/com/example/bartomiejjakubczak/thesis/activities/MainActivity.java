package com.example.bartomiejjakubczak.thesis.activities;

//TODO LOG: 05.09.18 database rules are changed to test mode and the app crashes without Internet
//TODO LOG CONTINUED: I successfully search for user and check his hasRoom value
//TODO LOG CONTINUED: Think of a way to present user with option to create a room if hasRoom is false
//TODO LOG 13.09.18: I think I will use 4 top-level nodes method
//TODO LOG CONTINUED: context of mainactivity has to be passed to alertdialog
//TODO LOG 21.09.18: think of some way to wait for update of hasRoom (or new technique as a whole)
//TODO LOG CONTINUED: next steps: show created room in a way you think of (in side panel or something)
//TODO LOG CONTINUED: next steps continued: get rid of hasRoom by searching for rooms in UserFlats if given user has any, then do activity transition in OnCompleteListener of CreateRoom in CreateFlatActivity
//TODO LOG 25.09.18: Replace word room with another word
//TODO LOG CONTINUED: Make suitable interfaces to cleanup your code
//TODO LOG 02.10.2018: Replace in firebase database names of flats and users with trues, rename last instances of wrong variable names (in FlatUsers, UserFlats) and make them Set instead of Array
//TODO LOG CONTINUED: Think of a way to group options in drawer (flat subgroups etc.)
//TODO LOG 03.10.18: Create option to join a flat instead of creating one
//TODO LOG CONTINUED: When adding new user generate autoamtically tag for him and let him edit it later in profile (for example promting a tip)
//TODO LOG CONTINUED: Think if it's necessary to add owner of flat (as a field in flats for example)
//TODO LOG 04.10.18: think of the way to cache recently used flat and allow users to switch them

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.dialogs.CreateFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements SharedPrefs{

    private static final String TAG = "MainActivity";

    private TextView mCurrentFlatName;
    private TextView mCurrentFlatAddress;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedUserReference;
    private DatabaseReference mSearchedUserFlatsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeFirebaseComponents();
        setContentView(R.layout.activity_main);
        setViews();
        setDrawer();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_user_email), user.getEmail().replaceAll("[\\s.]", ""));
                    initializeFirebaseDatabaseReferences(loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_user_email)));
                    decideToShowCreateFlatDialog();
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
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, "No flat yet");
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

    private void updateUI() {
        String currentFlatName = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name));
        String currentFlatAddress = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address));
        mCurrentFlatName.setText(currentFlatName);
        mCurrentFlatAddress.setText(currentFlatAddress);
        //TODO in the future: think of a way to look for other flats which the user is in in the case of "no flat yet"
    }

    /**
     * Initializes firebase components necessary for this activity.
     */

    private void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    /**
     * Initializes references to firebase database in order to query data to specific json tables.
     * @param dotlessEmail email address stripped out of dots necessary to look for specific user in database.
     *                     email address is the key of user table.
     */

    private void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        DatabaseReference mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mSearchedUserReference = mUsersDatabaseReference.child(dotlessEmail);
        DatabaseReference mUsersFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats));
        mSearchedUserFlatsReference = mUsersFlatsDatabaseReference.child(dotlessEmail);
    }

    /**
     * Sets all views visible inside this activity.
     */

    private void setViews() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        View mHeaderView = mNavigationView.getHeaderView(0);
        mToolbar = findViewById(R.id.toolbar);
        mCurrentFlatName = mHeaderView.findViewById(R.id.drawer_header_title);
        mCurrentFlatAddress = mHeaderView.findViewById(R.id.drawer_header_subtitle);
    }

    /**
     * Initializes the drawer
     */

    private void setDrawer() {
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //TODO response to multiple items in drawer
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                Log.d(TAG, "Drawer has been opened");
                updateUI();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                Log.d(TAG, "Drawer has been closed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void decideToShowCreateFlatDialog() {
        mSearchedUserFlatsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showCreateFlatAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showCreateFlatAlertDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        DialogFragment createFlatAlertDialog = new CreateFlatDialogFragment();
        createFlatAlertDialog.show(fragmentManager, getString(R.string.tags_create_flat_dialog));
    }
}
