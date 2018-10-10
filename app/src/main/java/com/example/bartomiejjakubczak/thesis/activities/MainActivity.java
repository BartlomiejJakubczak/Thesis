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
//TODO LOG 05.10.18: Check if everything works to this point: email verification, first login, account creation (if something goes wrong, check todo in LoginActivity)
//TODO LOG 08.10.18: you have to change Dialogs from builders to onCreateViews in order to set setCanceledOnTouchOutside to false
//TODO LOG CONTINUED: might change the create room dialog to some kind of activity to avoid future problems
//TODO LOG CONTINUED: deal with no flat yet in case of a situation in which user deletes app and there is no current flat in shared prefs
//TODO LOG CONTINUED: think of a way to not use static context of MainActivity (which still may be fine, because it's still in foreground during DialogFragment)
//TODO LOG CONTINUED: try to test deleting with 2 users, deleting may not work fine as of 08.10.18
//TODO LOG CONTINUED: try to deal with situation when user has a flat but he deleted the app and when he opens drawer for the first time he sees "No flat yet", when he opens for 2nd he will see correct result (reason: it takes a bit of time to look into database, so if you immediately open drawer it will show "no flat yet")
//TODO LOG 09.10.18: in the future disable necessary menu items when current flat is "no flat yet"
//TODO LOG CONTINUED: keep an eye out on setCurrentFlat() if its okay and if it works fine
//TODO LOG CONTINUED: Finish EditFlatActivity
//TODO LOG 10.10.18: IMPORTANTE!!!!!!!!!!! In the future, take care of firebase session being timed out due to inactivity

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
import com.example.bartomiejjakubczak.thesis.dialogs.DeleteFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPrefs, FirebaseConnection {

    private static final String TAG = "MainActivity";
    private static Context context;

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
    private DatabaseReference mUsersFlatsDatabaseReference;
    private DatabaseReference mFlatsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

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

    /**
     * Updates the UI based on flat selected by user
     */

    public static Context getContext() {
        return context;
    }

    private void updateUI() {
        String currentFlatName = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name));
        String currentFlatAddress = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address));
        mCurrentFlatName.setText(currentFlatName);
        mCurrentFlatAddress.setText(currentFlatAddress);
    }

    private void setCurrentFlat() {
        String currentFlatName = loadStringFromSharedPrefs(this, getString(R.string.shared_prefs_flat_name));
        final List<String> keys = new ArrayList<>();
        if (currentFlatName.equals("No flat yet") || currentFlatName.isEmpty()) {
            mSearchedUserFlatsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            keys.add(ds.getKey());
                        }
                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key), keys.get(0));
                        mFlatsDatabaseReference.child(loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key))).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String currentFlatName = dataSnapshot.child("name").getValue().toString();
                                String currentFlatAddress = dataSnapshot.child("address").getValue().toString();
                                putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name), currentFlatName);
                                putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address), currentFlatAddress);
                                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        showCreateFlatDialog();
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    /**
     * Initializes firebase components necessary for this activity.
     */

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    /**
     * Initializes references to firebase database in order to query data to specific json tables.
     * @param dotlessEmail email address stripped out of dots necessary to look for specific user in database.
     *                     email address is the key of user table.
     */

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        DatabaseReference mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mSearchedUserReference = mUsersDatabaseReference.child(dotlessEmail);
        mUsersFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats));
        mSearchedUserFlatsReference = mUsersFlatsDatabaseReference.child(dotlessEmail);
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
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
     * Initializes the drawer.
     */

    private void setDrawer() {
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_create_flat:
                        Intent intent = new Intent(getApplicationContext(), CreateFlatActivity.class);
                        startActivity(intent);
                        mDrawerLayout.closeDrawers();
                        return true;
                    case R.id.nav_delete_flat:
                        showDeleteFlatDialog();
                        mDrawerLayout.closeDrawers();
                        return true;
                    case R.id.nav_edit_profile:
                        Intent intentEditProfile = new Intent(getApplicationContext(), EditProfileActivity.class);
                        startActivity(intentEditProfile);
                        mDrawerLayout.closeDrawers();
                        return true;
                    case R.id.nav_edit_flat:
                        Intent intentEditFlat = new Intent(getApplicationContext(), EditFlatActivity.class);
                        startActivity(intentEditFlat);
                        mDrawerLayout.closeDrawers();
                }
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

    /**
     * If the user doesn't have any flat or doesn't belong to any, this method shows dialog which informs
     * user that he/she has to either create one or join one.
     */

    private void decideToShowCreateFlatDialog() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mSearchedUserFlatsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showCreateFlatDialog();
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                } else {
                    setCurrentFlat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showCreateFlatDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        DialogFragment createFlatAlertDialog = new CreateFlatDialogFragment();
        createFlatAlertDialog.show(fragmentManager, getString(R.string.tags_create_flat_dialog));
    }

    private void showDeleteFlatDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        DialogFragment deleteFlatAlertDialog = new DeleteFlatDialogFragment();
        deleteFlatAlertDialog.show(fragmentManager, getString(R.string.tags_delete_flat_dialog));
    }
}
