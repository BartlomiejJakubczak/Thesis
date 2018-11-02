package com.example.bartomiejjakubczak.thesis.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.activities.MainActivity;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.models.Flat;
import com.example.bartomiejjakubczak.thesis.models.RequestJoinNotification;
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

public class FlatsSearchFragmentAdapter extends RecyclerView.Adapter<FlatsSearchFragmentHolder> implements FirebaseConnection {

    private final Context context;
    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private ArrayList<Flat> flats = new ArrayList<>();
    private ArrayList<Flat> flatsCopy = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRequestSenderDatabaseReference;
    private DatabaseReference mRequestReceiverDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    public FlatsSearchFragmentAdapter(Context context, ArrayList<Flat> flats, ArrayList<Flat> flatsCopy) {
        this.context = context;
        this.flats.addAll(flats);
        this.flatsCopy.addAll(flatsCopy);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(userDotlessEmail);
    }

    public void filter(String text) {
        flats.clear();
        if (text.isEmpty()) {
           flats.addAll(flatsCopy);
        } else {
            text = text.toLowerCase().trim().replace(" ", "");
            for (Flat flat: flatsCopy) {
                String name = flat.getName().toLowerCase().trim().replace(" ", "");
                String address = flat.getAddress().toLowerCase().trim().replace(" ", "");
                String owner = flat.getOwner().toLowerCase().trim().replace(" ", "");
                if (name.contains(text) || address.contains(text) || owner.contains(text)) {
                    flats.add(flat);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void removeAt(int position) {
        flats.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, flats.size());
    }

    @NonNull
    @Override
    public FlatsSearchFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flat_search_model, parent, false);
        return new FlatsSearchFragmentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FlatsSearchFragmentHolder holder, int position) {
        final String flatOwnerKey = flats.get(position).getOwner();
        final String flatOwnerTag = flats.get(position).getOwnerTag();
        final String flatKey = flats.get(position).getKey();
        final String flatName = flats.get(position).getName();
        final String[] senderTag = new String[1];

        holder.flatName.setText(flats.get(position).getName());
        holder.flatAddress.setText(flats.get(position).getAddress());
        holder.flatOwner.setText(flats.get(position).getOwnerTag());
        holder.requestJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.requestJoin.setEnabled(false);
                mRequestSenderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        RequestJoinNotification requestJoinNotification = new RequestJoinNotification("Join flat", flatOwnerKey, flatOwnerTag, flatKey, flatName);
                        mRequestSenderDatabaseReference.child(requestJoinNotification.getKey()).setValue(requestJoinNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        senderTag[0] = dataSnapshot.getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mRequestReceiverDatabaseReference.child(flatOwnerKey).child("receivedNotifications").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        RequestJoinNotification requestJoinNotification = new RequestJoinNotification("Join flat", userDotlessEmail, senderTag[0], flatKey, flatName);
                        mRequestReceiverDatabaseReference.child(flatOwnerKey).child("receivedNotifications").child(requestJoinNotification.getKey()).setValue(requestJoinNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                removeAt(holder.getAdapterPosition());
                                Toast.makeText(MainActivity.getContext(), "The notification has been sent", Toast.LENGTH_SHORT).show();
                            }
                        });
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
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mRequestSenderDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child(dotlessEmail).child("sentNotifications");
        mRequestReceiverDatabaseReference = mFirebaseDatabase.getReference().child("notifications");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users").child(dotlessEmail).child("tag");
    }
}