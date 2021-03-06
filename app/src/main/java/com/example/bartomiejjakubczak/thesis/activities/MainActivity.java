package com.example.bartomiejjakubczak.thesis.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.fragments.AssignPersonFragment;
import com.example.bartomiejjakubczak.thesis.fragments.ChoreFragmentInfo;
import com.example.bartomiejjakubczak.thesis.fragments.ChoresFragment;
import com.example.bartomiejjakubczak.thesis.fragments.CreateChoreFragment;
import com.example.bartomiejjakubczak.thesis.fragments.CreateFlatFragment;
import com.example.bartomiejjakubczak.thesis.fragments.CreateFoodShareFragment;
import com.example.bartomiejjakubczak.thesis.fragments.CreateGroceryFragment;
import com.example.bartomiejjakubczak.thesis.fragments.EditProfileFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FlatSearchFragmentUpdated;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragment;
import com.example.bartomiejjakubczak.thesis.fragments.FoodShareFragmentInfo;
import com.example.bartomiejjakubczak.thesis.fragments.FunctionalitiesFragment;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryCompletedFragment;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryCompletedInfoFragment;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryListBoughtFragment;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryFragmentInfo;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryPendingFragment;
import com.example.bartomiejjakubczak.thesis.fragments.GroceryReceiptFragment;
import com.example.bartomiejjakubczak.thesis.fragments.ManageFlatFragment;
import com.example.bartomiejjakubczak.thesis.fragments.NotificationsFragment;
import com.example.bartomiejjakubczak.thesis.fragments.SwitchFlatFragment;
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

public class MainActivity extends AppCompatActivity implements SharedPrefs, FirebaseConnection {

    private static final String TAG = "MainActivity";
    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private static Context context;

    private TextView mCurrentFlatName;
    private TextView mCurrentFlatAddress;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    public static Toolbar mToolbar;
    private ActionBar mActionBar;
    private DrawerLayout.DrawerListener mDrawerListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ValueEventListener mFlatRemovalListener;
    private ValueEventListener mUserNotificationListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSearchedUserReference;
    private DatabaseReference mSearchedUserFlatsDatabaseReference;
    private DatabaseReference mUsersFlatsDatabaseReference;
    private DatabaseReference mFlatsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mCurrentUserNotificationsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);
        setContentView(R.layout.activity_main);
        setViews();
        setDrawer();
        setCurrentUserFlatsPrefs();
        setListenerForNotifications();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // TODO THIS GETS CALLED EVERY TIME USER LEAVES APP/RETURNS TO IT
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String cachedUserEmail = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_user_email));
                    if (!currentUserEmail.equals(cachedUserEmail)) {
                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_name), getString(R.string.shared_prefs_default));
                        putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_address), getString(R.string.shared_prefs_default));
                    }
                    putStringToSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_user_email), currentUserEmail);
                } else {
                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    finish();
                    startActivity(intent);
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
            case R.id.notifications_menu:
                FragmentManager notificationsFragmentManager = getFragmentManager();
                FragmentTransaction notificationsFragmentTransaction = notificationsFragmentManager.beginTransaction();
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                notificationsFragmentTransaction.replace(R.id.fragment_placeholder, notificationsFragment, "notificationsFragment");
                notificationsFragmentManager.popBackStack("notificationsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                notificationsFragmentTransaction.commit();
                return true;
            case R.id.home_menu:
                setFunctionalitiesFragmentNoBacktrace();
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

    private void setListenerForNotifications() {
        mUserNotificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean flag = true;
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (!(boolean)ds.child("seen").getValue()) {
                        flag = false;
                    }
                }
                if (!flag) {
                    Drawable drawable = ContextCompat
                            .getDrawable(getApplicationContext(),R.drawable.ic_notifications);
                    mToolbar.getMenu().getItem(2).setIcon(drawable).setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mCurrentUserNotificationsDatabaseReference.addValueEventListener(mUserNotificationListener);
    }

    private void setListenerForFlatRemoval() {

        mFlatRemovalListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> flatKeys = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    flatKeys.add(ds.getKey());
                }
                if (flatKeys.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    if (!flatKeys.contains(loadStringFromSharedPrefs(getApplicationContext(), "flat_key"))) {
                        resetFragments();
                        Toast.makeText(getApplicationContext(), "You have been removed from room " + mCurrentFlatName.getText(), Toast.LENGTH_SHORT).show();
                        putStringToSharedPrefs(getApplicationContext(), "flat_key", flatKeys.get(0));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mUsersFlatsDatabaseReference.child(currentUserEmail).addValueEventListener(mFlatRemovalListener);
    }

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
                        createFragmentTransaction.replace(R.id.fragment_placeholder, flatCreateFragment, "flatCreateFragment");
                        createFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                    case R.id.nav_edit_profile:
                        FragmentManager editProfileFragmentManager = getFragmentManager();
                        FragmentTransaction editProfileFragmentTransaction = editProfileFragmentManager.beginTransaction();
                        EditProfileFragment editProfileFragment = new EditProfileFragment();
                        editProfileFragmentTransaction.replace(R.id.fragment_placeholder, editProfileFragment, "profileEditFragment");
                        editProfileFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                    case R.id.nav_manage_flat:
                        FragmentManager manageFragmentManager = getFragmentManager();
                        FragmentTransaction manageFragmentTransaction = manageFragmentManager.beginTransaction();
                        ManageFlatFragment manageFlatFragment = new ManageFlatFragment();
                        manageFragmentTransaction.replace(R.id.fragment_placeholder, manageFlatFragment, "manageFlatFragment");
                        manageFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                    case R.id.nav_switch_flat:
                        FragmentManager switchFragmentManager = getFragmentManager();
                        FragmentTransaction switchFragmentTransaction = switchFragmentManager.beginTransaction();
                        SwitchFlatFragment switchFlatFragment = new SwitchFlatFragment();
                        switchFragmentTransaction.replace(R.id.fragment_placeholder, switchFlatFragment, "switchFlatFragment");
                        switchFragmentTransaction.commit();
                        mDrawerLayout.closeDrawer(Gravity.START, true);
                        return true;
                    case R.id.nav_find_flat:
                        FragmentManager findFragmentManager = getFragmentManager();
                        FragmentTransaction findFragmentTransaction = findFragmentManager.beginTransaction();
                        FlatSearchFragmentUpdated flatSearchFragment = new FlatSearchFragmentUpdated();
                        findFragmentTransaction.replace(R.id.fragment_placeholder, flatSearchFragment, "flatSearchFragment");
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
                InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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

    private void updateUI() {
        mFlatsDatabaseReference
                .child(loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key)))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!mCurrentFlatName.getText().equals(dataSnapshot.child("name").getValue().toString()) || !mCurrentFlatAddress.getText().equals(dataSnapshot.child("address").getValue().toString())) {
                    mCurrentFlatName.setText(dataSnapshot.child("name").getValue().toString());
                    mCurrentFlatAddress.setText(dataSnapshot.child("address").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setFunctionalitiesFragment() {
        FunctionalitiesFragment functionalitiesFragment = new FunctionalitiesFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_placeholder, functionalitiesFragment, "functionalitiesFragment");
        fragmentTransaction.commit();
    }

    private void setFunctionalitiesFragmentNoBacktrace() {
        FunctionalitiesFragment functionalitiesFragment = new FunctionalitiesFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_placeholder, functionalitiesFragment, "functionalitiesFragment");
        getFragmentManager().popBackStack("functionalitiesFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

    private void resetFragments() {
        CreateFlatFragment createFlatFragment = (CreateFlatFragment) getFragmentManager().findFragmentByTag("flatCreateFragment");
        FlatSearchFragmentUpdated flatSearchFragment = (FlatSearchFragmentUpdated) getFragmentManager().findFragmentByTag("flatSearchFragment");
        ManageFlatFragment manageFlatFragment = (ManageFlatFragment) getFragmentManager().findFragmentByTag("manageFlatFragment");
        EditProfileFragment editProfileFragment = (EditProfileFragment) getFragmentManager().findFragmentByTag("profileEditFragment");
        NotificationsFragment notificationsFragment = (NotificationsFragment) getFragmentManager().findFragmentByTag("notificationsFragment");
        SwitchFlatFragment switchFlatFragment = (SwitchFlatFragment) getFragmentManager().findFragmentByTag("switchFlatFragment");
        FoodShareFragment foodShareFragment = (FoodShareFragment) getFragmentManager().findFragmentByTag("foodShareFragment");
        CreateFoodShareFragment createFoodShareFragment = (CreateFoodShareFragment) getFragmentManager().findFragmentByTag("createFoodShareFragment");
        FoodShareFragmentInfo foodShareFragmentInfo = (FoodShareFragmentInfo) getFragmentManager().findFragmentByTag("foodShareFragmentInfo");
        GroceryPendingFragment groceryPendingFragment = (GroceryPendingFragment) getFragmentManager().findFragmentByTag("groceryPendingFragment");
        CreateGroceryFragment createGroceryFragment = (CreateGroceryFragment) getFragmentManager().findFragmentByTag("createGroceryFragment");
        GroceryFragmentInfo groceryFragmentInfo = (GroceryFragmentInfo) getFragmentManager().findFragmentByTag("groceryFragmentInfo");
        GroceryListBoughtFragment groceryListBoughtFragment = (GroceryListBoughtFragment) getFragmentManager().findFragmentByTag("groceryListBoughtFragment");
        GroceryCompletedFragment groceryCompletedFragment = (GroceryCompletedFragment) getFragmentManager().findFragmentByTag("groceryCompletedFragment");
        GroceryCompletedInfoFragment groceryCompletedInfoFragment = (GroceryCompletedInfoFragment) getFragmentManager().findFragmentByTag("groceryCompletedInfoFragment");
        GroceryReceiptFragment groceryReceiptFragment = (GroceryReceiptFragment) getFragmentManager().findFragmentByTag("groceryReceiptFragment");

        ChoresFragment choresFragment = (ChoresFragment) getFragmentManager().findFragmentByTag("choresFragment");
        ChoreFragmentInfo choreFragmentInfo = (ChoreFragmentInfo) getFragmentManager().findFragmentByTag("choreFragmentInfo");
        CreateChoreFragment createChoreFragment = (CreateChoreFragment) getFragmentManager().findFragmentByTag("createChoreFragment");
        AssignPersonFragment assignPersonFragment = (AssignPersonFragment) getFragmentManager().findFragmentByTag("assignPersonFragment");

        if (flatSearchFragment != null && flatSearchFragment.isVisible()) {
            setFunctionalitiesFragment();
        } else if (createFlatFragment != null && createFlatFragment.isVisible()) {
            setFunctionalitiesFragment();
        } else if (manageFlatFragment != null && manageFlatFragment.isVisible()) {
            setFunctionalitiesFragment();
        } else if (editProfileFragment != null && editProfileFragment.isVisible()) {
            setFunctionalitiesFragment();
        } else if (notificationsFragment != null && notificationsFragment.isVisible()) {
            setFunctionalitiesFragment();
        } else if (switchFlatFragment != null && switchFlatFragment.isVisible()) {
            setFunctionalitiesFragment();
        } else if (foodShareFragment != null && foodShareFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (createFoodShareFragment != null && createFoodShareFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (foodShareFragmentInfo != null && foodShareFragmentInfo.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (groceryPendingFragment != null && groceryPendingFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (createGroceryFragment != null && createGroceryFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (groceryFragmentInfo != null && groceryFragmentInfo.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (groceryListBoughtFragment != null && groceryListBoughtFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (groceryCompletedFragment != null && groceryCompletedFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (groceryCompletedInfoFragment != null && groceryCompletedInfoFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (groceryReceiptFragment != null && groceryReceiptFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (choresFragment != null && choresFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (choreFragmentInfo != null && choreFragmentInfo.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (createChoreFragment != null && createChoreFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        } else if (assignPersonFragment != null && assignPersonFragment.isVisible()) {
            setFunctionalitiesFragmentNoBacktrace();
        }
    }

    private void setCurrentUserFlatsPrefs() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        final ArrayList<String> currentSearchedUserFlatsKeys = new ArrayList<>();
        String currentFlatKey = loadStringFromSharedPrefs(getApplicationContext(), getString(R.string.shared_prefs_flat_key));
        if (currentFlatKey.isEmpty() || currentFlatKey.equals("No flat yet")) {
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
                            putStringToSharedPrefs(MainActivity.getContext(), getString(R.string.shared_prefs_flat_key), ds.child("key").getValue().toString());
                            if (currentUserEmail.equals(ds.child("owner").getValue().toString())) {
                                putStringToSharedPrefs(getApplicationContext(), "shared_prefs_is_owner", "yes");
                            } else {
                                putStringToSharedPrefs(getApplicationContext(), "shared_prefs_is_owner", "no");
                            }
                            break;
                        }
                    }
                    setFunctionalitiesFragment();
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                    setListenerForFlatRemoval();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            setFunctionalitiesFragment();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            setListenerForFlatRemoval();
        }
    }

    private void setViews() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        View mHeaderView = mNavigationView.getHeaderView(0);
        mToolbar = findViewById(R.id.toolbar);
        mCurrentFlatName = mHeaderView.findViewById(R.id.drawer_header_title);
        mCurrentFlatAddress = mHeaderView.findViewById(R.id.drawer_header_subtitle);
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_users));
        mCurrentUserNotificationsDatabaseReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(currentUserEmail)
                .child("notifications");
        mSearchedUserReference = mUsersDatabaseReference.child(dotlessEmail);
        mUsersFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_reference_user_flats));
        mSearchedUserFlatsDatabaseReference = mUsersFlatsDatabaseReference.child(dotlessEmail);
        mFlatsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.firebase_references_flats));
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(label, string).apply();
    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        putStringToSharedPrefs(getApplicationContext(), "flat_key", "No flat yet");
        if (mFlatRemovalListener != null) {
            mUsersFlatsDatabaseReference.child(currentUserEmail).removeEventListener(mFlatRemovalListener);
        }
        if (mUserNotificationListener != null) {
            mCurrentUserNotificationsDatabaseReference.removeEventListener(mUserNotificationListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
