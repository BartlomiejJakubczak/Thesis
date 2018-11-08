package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.SharedPrefs;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FlatsAdapter extends RecyclerView.Adapter<FlatsHolder> implements SharedPrefs, FirebaseConnection {

    Context context;
    private ArrayList<Flat> flats = new ArrayList<>();
    private final String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFlatsDatabaseReference;


    public FlatsAdapter(Context context, ArrayList<Flat> flats) {
        this.context = context;
        this.flats.addAll(flats);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(currentUserEmail);
    }

    @NonNull
    @Override
    public FlatsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flat_model, parent, false);
        return new FlatsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlatsHolder holder, int position) {
        final String flatName = flats.get(position).getName();
        final String flatAddress = flats.get(position).getAddress();
        final String flatKey = flats.get(position).getKey();

        holder.flatName.setText(flats.get(position).getName());
        holder.flatAddress.setText(flats.get(position).getAddress());
        holder.flatKey = flats.get(position).getKey();
        holder.switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (currentUserEmail.equals(dataSnapshot.child(flatKey).child("owner").getValue().toString())) {
                            putStringToSharedPrefs(MainActivity.getContext(), "shared_prefs_is_owner", "yes");
                            putStringToSharedPrefs(MainActivity.getContext(), "flat_name", flatName);
                            putStringToSharedPrefs(MainActivity.getContext(), "flat_address", flatAddress);
                            putStringToSharedPrefs(MainActivity.getContext(), "flat_key", flatKey);
                        } else {
                            putStringToSharedPrefs(MainActivity.getContext(), "shared_prefs_is_owner", "no");
                            putStringToSharedPrefs(MainActivity.getContext(), "flat_name", flatName);
                            putStringToSharedPrefs(MainActivity.getContext(), "flat_address", flatAddress);
                            putStringToSharedPrefs(MainActivity.getContext(), "flat_key", flatKey);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return flats.size();
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
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mFlatsDatabaseReference = mFirebaseDatabase.getReference()
                .child("flats");
    }
}
