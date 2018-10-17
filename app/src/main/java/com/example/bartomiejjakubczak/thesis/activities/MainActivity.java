package com.example.bartomiejjakubczak.thesis.activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.example.bartomiejjakubczak.thesis.dialogs.CreateFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.dialogs.DeleteFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.dialogs.SwitchFlatDialogFragment;
import com.example.bartomiejjakubczak.thesis.interfaces.DeleteDialogCloseListener;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.utilities.TinyDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPrefs, FirebaseConnection, DeleteDialogCloseListener {

    private static final String TAG = "MainActivity";
    private static Context context;

    private TextView mCurrentFlatName;
    private TextView mCurrentFlatAddress;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private TinyDB tinyDB;

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
        tinyDB = new TinyDB(getApplicationContext());

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
                        mDrawerLayout.closeDrawer(Gravity.START, false);
                        return true;
                    case R.id.nav_delete_flat:
                        if (tinyDB.getListString(getString(R.string.shared_prefs_list_flat_names)).size() - 1 != 0) {
                            showDeleteFlatDialog();
                            mDrawerLayout.closeDrawer(Gravity.START, false);
                            return true;
                        } else {
                            //TODO dialog which informs you are about to delete the last flat, delete the flat from db and pop createdialog
                        }
                        return true;
                    case R.id.nav_edit_profile:
                        Intent intentEditProfile = new Intent(getApplicationContext(), EditProfileActivity.class);
                        startActivity(intentEditProfile);
                        mDrawerLayout.closeDrawer(Gravity.START, false);
                        return true;
                    case R.id.nav_edit_flat:
                        Intent intentEditFlat = new Intent(getApplicationContext(), EditFlatActivity.class);
                        startActivity(intentEditFlat);
                        mDrawerLayout.closeDrawer(Gravity.START, false);
                        return true;
                    case R.id.nav_switch_flat:
                        showSwitchFlatDialog();
                        mDrawerLayout.closeDrawer(Gravity.START, false);
                        return true;
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
                updateUI();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
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

    private void setCurrentFlat() {
        String currentFlatName = loadStringFromSharedPrefs(this, getString(R.string.shared_prefs_flat_name));
        if (currentFlatName.equals(getString(R.string.shared_prefs_default)) || currentFlatName.isEmpty()) {
            final ArrayList<String> keys = new ArrayList<>();
            final ArrayList<String> names = new ArrayList<>();
            final ArrayList<String> addresses = new ArrayList<>();
            final ArrayList<String> owners = new ArrayList<>();
            mSearchedUserFlatsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        keys.add(ds.getKey());
                    }
                    putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key), keys.get(0));
                    mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                String currentFlatName = ds.child(getString(R.string.flat_node_name)).getValue().toString();
                                String currentFlatAddress = ds.child(getString(R.string.flat_node_address)).getValue().toString();
                                String currentFlatKey = ds.child(getString(R.string.flat_node_key)).getValue().toString();
                                String currentFlatOwner = ds.child(getString(R.string.flat_node_owner)).getValue().toString();
                                names.add(currentFlatName);
                                addresses.add(currentFlatAddress);
                                owners.add(currentFlatOwner);
                                    if (currentFlatKey.equals(loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key)))) {
                                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name), currentFlatName);
                                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address), currentFlatAddress);
                                    }
                            }
                            tinyDB.putListString(getString(R.string.shared_prefs_list_flat_names), names);
                            tinyDB.putListString(getString(R.string.shared_prefs_list_flat_addresses), addresses);
                            tinyDB.putListString(getString(R.string.shared_prefs_list_flat_keys), keys);
                            tinyDB.putListString(getString(R.string.shared_prefs_list_flat_owners), owners);
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final ArrayList<String> keys = new ArrayList<>();
                    final ArrayList<String> names = new ArrayList<>();
                    final ArrayList<String> addresses = new ArrayList<>();
                    final ArrayList<String> owners = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        names.add(ds.child(getString(R.string.flat_node_name)).getValue().toString());
                        addresses.add(ds.child(getString(R.string.flat_node_address)).getValue().toString());
                        keys.add(ds.child(getString(R.string.flat_node_key)).getValue().toString());
                        owners.add(ds.child(getString(R.string.flat_node_owner)).getValue().toString());
                    }
                    tinyDB.putListString(getString(R.string.shared_prefs_list_flat_names), names);
                    tinyDB.putListString(getString(R.string.shared_prefs_list_flat_addresses), addresses);
                    tinyDB.putListString(getString(R.string.shared_prefs_list_flat_keys), keys);
                    tinyDB.putListString(getString(R.string.shared_prefs_list_flat_owners), owners);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        TinyDB tinyDB = new TinyDB(getApplicationContext());
        ArrayList<String> flatNames = tinyDB.getListString("shared_prefs_list_flat_names");
        ArrayList<String> flatAddresses = tinyDB.getListString("shared_prefs_list_flat_addresses");
        ArrayList<String> flatKeys = tinyDB.getListString("shared_prefs_list_flat_keys");
        flatNames.remove(loadStringFromSharedPrefs(MainActivity.getContext(), "flat_name"));
        flatAddresses.remove(loadStringFromSharedPrefs(MainActivity.getContext(), "flat_address"));
        flatKeys.remove(loadStringFromSharedPrefs(MainActivity.getContext(), "flat_key"));
        putStringToSharedPrefs(MainActivity.getContext(), "flat_name", flatNames.get(flatNames.size() - 1));
        putStringToSharedPrefs(MainActivity.getContext(), "flat_address", flatAddresses.get(flatAddresses.size() - 1));
        putStringToSharedPrefs(MainActivity.getContext(), "flat_key", flatKeys.get(flatKeys.size() - 1));
        tinyDB.putListString("shared_prefs_list_flat_names", flatNames);
        tinyDB.putListString("shared_prefs_list_flat_addresses", flatAddresses);
        tinyDB.putListString("shared_prefs_list_flat_keys", flatKeys);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
