package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.adapters.GroceryFragmentPendingAdapter;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroceryPendingFragment extends Fragment implements FirebaseConnection, SharedPrefs {

    private String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private GroceryPendingFragment fragment;

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;
    public FloatingActionButton fabBought;
    private RecyclerView groceryPendingRecyclerView;
    private GroceryFragmentPendingAdapter groceryFragmentPendingAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private ValueEventListener mPendingGroceriesListener;
    private DatabaseReference mPendingGroceryDatabaseReference;

    private void setListeners() {
        Log.i("GroceryPendingFragment", "setListeners got called");
        mPendingGroceriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<GroceryItem> groceryItems = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    groceryItems.add(new GroceryItem(
                            ds.child("key").getValue().toString(),
                            ds.child("name").getValue().toString(),
                            ds.child("quantity").getValue().toString(),
                            ds.child("notes").getValue().toString(),
                            ds.child("addingPersonKey").getValue().toString(),
                            ds.child("date").getValue().toString()));
                }
                if (groceryFragmentPendingAdapter == null) {
                    groceryFragmentPendingAdapter = new GroceryFragmentPendingAdapter(getActivity(), fragment, groceryItems);
                    groceryPendingRecyclerView.setAdapter(groceryFragmentPendingAdapter);
                } else {
                    groceryFragmentPendingAdapter.receiveNewList(groceryItems);
                    groceryPendingRecyclerView.setAdapter(groceryFragmentPendingAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mPendingGroceryDatabaseReference.addValueEventListener(mPendingGroceriesListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = this;
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        setListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPendingGroceryDatabaseReference.removeEventListener(mPendingGroceriesListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_pending, container, false);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation_pending);
        fabAdd = view.findViewById(R.id.grocery_fab);
        fabBought = view.findViewById(R.id.bought_fab);
        groceryPendingRecyclerView = view.findViewById(R.id.grocery_recyclerView);
        groceryPendingRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_pending:
                        break;
                    case R.id.action_completed:
                        GroceryCompletedFragment groceryCompletedFragment = new GroceryCompletedFragment();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_placeholder, groceryCompletedFragment, "groceryCompletedFragment");
                        getFragmentManager().popBackStack();
                        fragmentTransaction.addToBackStack("functionalitiesFragment");
                        fragmentTransaction.commit();
                }
                return false;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_pending);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateGroceryFragment createGroceryFragment = new CreateGroceryFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, createGroceryFragment, "createGroceryFragment");
                fragmentTransaction.addToBackStack("groceryPendingFragment");
                fragmentTransaction.commit();
            }
        });
        fabBought.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle boughtItemsIDs = new Bundle();
                boughtItemsIDs.putStringArrayList("bought_items_ids", groceryFragmentPendingAdapter.getBoughtItemsIDs());
                Log.i("GroceryPendingFragment", groceryFragmentPendingAdapter.getBoughtItemsIDs().toString());
                GroceryListBoughtFragment groceryListBoughtFragment = new GroceryListBoughtFragment();
                groceryListBoughtFragment.setArguments(boughtItemsIDs);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, groceryListBoughtFragment, "groceryListBoughtFragment");
                fragmentTransaction.addToBackStack("groceryPendingFragment");
                fragmentTransaction.commit();
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
        mPendingGroceryDatabaseReference = mFirebaseDatabase
                .getReference()
                .child("grocery")
                .child("pendingGrocery")
                .child(loadStringFromSharedPrefs(getActivity(), getString(R.string.shared_prefs_flat_key)));
    }

    @Override
    public void putStringToSharedPrefs(Context context, String label, String string) {

    }

    @Override
    public String loadStringFromSharedPrefs(Context context, String label) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(label, getString(R.string.shared_prefs_default));
    }
}
