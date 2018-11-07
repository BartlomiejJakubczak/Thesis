package com.example.bartomiejjakubczak.thesis.activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.FlatsSearchFragmentAdapter;
import com.example.bartomiejjakubczak.thesis.dialogs.CreateFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.dialogs.DeleteFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.dialogs.SwitchFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.fragments.CreateFlatFragment;
import com.example.bartomiejjakubczak.thesis.fragments.EditFlatFragment;
import com.example.bartomiejjakubczak.thesis.fragments.EditProfileFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FlatSearchFragment;
import com.example.bartomiejjakubczak.thesis.fragments.ManageFlatFragment;
import com.example.bartomiejjakubczak.thesis.fragments.NotificationsFragment;
import com.example.bartomiejjakubczak.thesis.interfaces.DeleteDialogCloseListener;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.example.bartomiejjakubczak.thesis.utilities.TinyDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SharedPrefs, FirebaseConnection, DeleteDialogCloseListener {

    private static final String TAG = "MainActivity";
    private String currentUserEmail;
    private static Context context;

    private TextView mCurrentFlatName;
    private TextView mCurrentFlatAddress;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private DrawerLayout.DrawerListener mDrawerListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedUserReference;
    private DatabaseReference mSearchedUserFlatsDatabaseReference;
    private DatabaseReference mUsersFlatsDatabaseReference;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate was called");
        context = this;
        initializeFirebaseComponents();
        setContentView(R.layout.activity_main);
        setViews();
        setDrawer();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // TODO THIS GETS CALLED EVERY TIME USER LEAVES APP/RETURNS TO IT
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    currentUserEmail = user.getEmail().replaceAll("[\\s.]", "");
                    String cachedUserEmail = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_user_email));
                    if (!currentUserEmail.equals(cachedUserEmail)) {
                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name), getString(R.string.shared_prefs_default));
                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address), getString(R.string.shared_prefs_default));
                    }
                    putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_user_email), currentUserEmail);
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

    // ------------------------------------------MENUS AND DRAWER---------------------------------------------

    // creates menu which holds button to sign off

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // assigns action to buttons on the main menu, so signing off and pressing drawer hamburger icon

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                return true;
            case R.id.notifications_menu:
                // TODO has to transmit between other fragments in drawers
                FragmentManager notificationsFragmentManager = getFragmentManager();
                FragmentTransaction notificationsFragmentTransaction = notificationsFragmentManager.beginTransaction();
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                notificationsFragmentTransaction.replace(R.id.fragment_placeholder, notificationsFragment, "notificationsFragment");
                notificationsFragmentTransaction.commit();
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

    /**
     * Initializes the drawer.
     */

    private void setDrawer() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_create_flat:
                        FragmentManager createFragmentManager = getFragmentManager();
                        FragmentTransaction createFragmentTransaction = createFragmentManager.beginTransaction();
                        CreateFlatFragment flatCreateFragment = new CreateFlatFragment();
                        createFragmentTransaction.add(R.id.fragment_placeholder, flatCreateFragment, "flatCreateFragment");
                        createFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
//                    case R.id.nav_delete_flat:
//                        showDeleteFlatDialog();
//                        mDrawerLayout.closeDrawer(Gravity.START, true);
//                        return true;
                    case R.id.nav_edit_profile:
                        FragmentManager editProfileFragmentManager = getFragmentManager();
                        FragmentTransaction editProfileFragmentTransaction = editProfileFragmentManager.beginTransaction();
                        EditProfileFragment editProfileFragment = new EditProfileFragment();
                        editProfileFragmentTransaction.add(R.id.fragment_placeholder, editProfileFragment, "profileEditFragment");
                        editProfileFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                    case R.id.nav_manage_flat:
                        FragmentManager manageFragmentManager = getFragmentManager();
                        FragmentTransaction manageFragmentTransaction = manageFragmentManager.beginTransaction();
                        ManageFlatFragment manageFlatFragment = new ManageFlatFragment();
                        manageFragmentTransaction.add(R.id.fragment_placeholder, manageFlatFragment, "manageFlatFragment");
                        manageFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                    case R.id.nav_switch_flat:
                        showSwitchFlatDialog();
                        mDrawerLayout.closeDrawer(Gravity.START, false);
                        return true;
                    case R.id.nav_find_flat:
                        FragmentManager findFragmentManager = getFragmentManager();
                        FragmentTransaction findFragmentTransaction = findFragmentManager.beginTransaction();
                        FlatSearchFragment flatSearchFragment = new FlatSearchFragment();
                        findFragmentTransaction.add(R.id.fragment_placeholder, flatSearchFragment, "flatSearchFragment");
                        findFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                }
                return true;
            }
        });

        mDrawerListener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                resetFragments();
                updateUI();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };
        mDrawerLayout.addDrawerListener(mDrawerListener);
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }

    /**
     * Updates the UI based on flat selected by user
     */

    private void updateUI() {
        String currentFlatName = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name));
        String currentFlatAddress = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address));
        mCurrentFlatName.setText(currentFlatName);
        mCurrentFlatAddress.setText(currentFlatAddress);
    }

    private void resetFragments() {
        CreateFlatFragment createFlatFragment = (CreateFlatFragment) getFragmentManager().findFragmentByTag("flatCreateFragment");
        FlatSearchFragment flatSearchFragment = (FlatSearchFragment) getFragmentManager().findFragmentByTag("flatSearchFragment");
        ManageFlatFragment manageFlatFragment = (ManageFlatFragment) getFragmentManager().findFragmentByTag("manageFlatFragment");
        EditProfileFragment editProfileFragment = (EditProfileFragment) getFragmentManager().findFragmentByTag("profileEditFragment");
        NotificationsFragment notificationsFragment = (NotificationsFragment) getFragmentManager().findFragmentByTag("notificationsFragment");

        if (flatSearchFragment != null && flatSearchFragment.isVisible()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(flatSearchFragment);
            fragmentTransaction.commit();
        } else if (createFlatFragment != null && createFlatFragment.isVisible()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(createFlatFragment);
            fragmentTransaction.commit();
        } else if (manageFlatFragment != null && manageFlatFragment.isVisible()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(manageFlatFragment);
            fragmentTransaction.commit();
        } else if (editProfileFragment != null && editProfileFragment.isVisible()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(editProfileFragment);
            fragmentTransaction.commit();
        } else if (notificationsFragment != null && notificationsFragment.isVisible()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(notificationsFragment);
            fragmentTransaction.commit();
        }
    }

    private void setCurrentUserFlatsPrefs() {
        Log.d(TAG, "setCurrentUserFlatsPrefs was called");
        final ArrayList<String> currentSearchedUserFlatsKeys = new ArrayList<>();
        String currentFlatName = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name));
        if (currentFlatName.isEmpty() || currentFlatName.equals("No flat yet")) {
            mSearchedUserFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        currentSearchedUserFlatsKeys.add(ds.getKey());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        if (currentSearchedUserFlatsKeys.contains(ds.getKey())) {
                            putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_name), ds.child("name").getValue().toString());
                            putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_address), ds.child("address").getValue().toString());
                            putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_key), ds.child("key").getValue().toString());
                            break;
                        }
                    }
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
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

    // ------------------------------------DIALOGS-------------------------------------------

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

    @Override
    public void handleDeleteDialogClose() {

        //TODO HANDLE THE SITUATION WHEN USER IS DELETING HIS LAST FLAT
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        final ArrayList<String> currentSearchedUserFlatsKeys = new ArrayList<>();
        mSearchedUserFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    currentSearchedUserFlatsKeys.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (currentSearchedUserFlatsKeys.contains(ds.getKey())) {
                        putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_name), ds.child("name").getValue().toString());
                        putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_address), ds.child("address").getValue().toString());
                        putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_key), ds.child("key").getValue().toString());
                        mActionBar.setDisplayHomeAsUpEnabled(true);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showSwitchFlatDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        DialogFragment switchFlatAlertDialog = new SwitchFlatDialogFragment();
        switchFlatAlertDialog.show(fragmentManager, getString(R.string.tags_switch_flat_dialog));
    }

    /**
     * If the user doesn't have any flat or doesn't belong to any, this method shows dialog which informs
     * user that he/she has to either create one or join one.
     */

    private void decideToShowCreateFlatDialog() {
        Log.d(TAG, "decideToShowCreateFlatDialog was called");
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mUsersDatabaseReference.child(currentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                putStringToSharedPrefs(getApplicationContext(), "shared_prefs_user_tag", dataSnapshot.child("tag").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSearchedUserFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showCreateFlatDialog();
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                } else {
                    setCurrentUserFlatsPrefs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Initializes references to firebase database in order to query data to specific json tables.
     * @param dotlessEmail email address stripped out of dots necessary to look for specific user in database.
     *                     email address is the key of user table.
     */

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mSearchedUserReference = mUsersDatabaseReference.child(dotlessEmail);
        mUsersFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats));
        mSearchedUserFlatsDatabaseReference = mUsersFlatsDatabaseReference.child(dotlessEmail);
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
    }

    /**
     * Initializes firebase components necessary for this activity.
     */

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        mDrawerLayout.addDrawerListener(mDrawerListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mDrawerLayout.removeDrawerListener(mDrawerListener);
    }
}
