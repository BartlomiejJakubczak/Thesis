package com.example.bartomiejjakubczak.thesis.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.ListNotifications;
import com.example.bartomiejjakubczak.thesis.models.AddedChoreNotification;
import com.example.bartomiejjakubczak.thesis.models.AddedFoodShareNotification;
import com.example.bartomiejjakubczak.thesis.models.AddedGroceryNotification;
import com.example.bartomiejjakubczak.thesis.models.CompletedGroceryList;
import com.example.bartomiejjakubczak.thesis.models.RequestJoinNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestsFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FirebaseConnection {

    private final Context context;
    private String userDotlessEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replaceAll("[\\s.]", "");
    private ArrayList<ListNotifications> notifications = new ArrayList<>();
    private ArrayList<ListNotifications> notificationsCopy = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSentNotificationsDatabaseReference;
    private DatabaseReference mReceivedNotificationsDatabaseReference;
    private DatabaseReference mUserFlatsDatabaseReference;
    private DatabaseReference mFlatUsersDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    public RequestsFragmentAdapter(Context context, ArrayList<ListNotifications> notifications, ArrayList<ListNotifications> notificationsCopy) {
        this.context = context;
        this.notifications.addAll(notifications);
        this.notificationsCopy.addAll(notificationsCopy);
        initializeFirebaseComponents();
        initializeFirebaseDatabaseReferences(userDotlessEmail);
    }

    private void removeAt(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, notifications.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ListNotifications.TYPE_JOIN_NOTIFICATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.join_notification_model, parent, false);
                return new RequestJoinFragmentHolder(view);
            case ListNotifications.TYPE_FOODSHARE_ADDED_NOTIFICATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.foodshare_added_model, parent, false);
                return new AddedFoodshareHolder(view);
            case ListNotifications.TYPE_GROCERY_ADDED_NOTIFICATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_added_model, parent, false);
                return new AddedGroceryHolder(view);
            case ListNotifications.TYPE_CHORE_ADDED_NOTIFICATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chore_added_model, parent, false);
                return new AddedChoreHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case ListNotifications.TYPE_JOIN_NOTIFICATION:
                final RequestJoinNotification requestJoinNotification = (RequestJoinNotification) notifications.get(position);
                final RequestJoinFragmentHolder joinFragmentHolder = (RequestJoinFragmentHolder)holder;

                joinFragmentHolder.title.setText("JOIN REQUEST");
                joinFragmentHolder.message.setText("User " + requestJoinNotification.getPersonInvolvedTag() + " wants to join your flat "
                + requestJoinNotification.getFlatInvolvedName() + ".");
                joinFragmentHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(holder.getAdapterPosition());
                        mFlatUsersDatabaseReference.child(requestJoinNotification.getFlatInvolvedKey()).child(requestJoinNotification.getPersonInvolvedKey()).setValue(true);
                        mUserFlatsDatabaseReference.child(requestJoinNotification.getPersonInvolvedKey()).child(requestJoinNotification.getFlatInvolvedKey()).setValue(true);
                        mSentNotificationsDatabaseReference.child(requestJoinNotification.getSentNotificationKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mReceivedNotificationsDatabaseReference.child(requestJoinNotification.getKey()).removeValue();
                            }
                        });
                    }
                });
                joinFragmentHolder.declineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(holder.getAdapterPosition());
                        mSentNotificationsDatabaseReference.child(requestJoinNotification.getSentNotificationKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mReceivedNotificationsDatabaseReference.child(requestJoinNotification.getKey()).removeValue();
                            }
                        });
                    }
                });
                break;
            case ListNotifications.TYPE_FOODSHARE_ADDED_NOTIFICATION:
                final AddedFoodShareNotification addedFoodShareNotification = (AddedFoodShareNotification) notifications.get(position);
                final AddedFoodshareHolder addedFoodshareHolder = (AddedFoodshareHolder) holder;

                mUsersDatabaseReference.child(addedFoodShareNotification.getSenderKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        addedFoodshareHolder.title.setText("FOODSHARE");
                        addedFoodshareHolder.message.setText("User " + dataSnapshot.child("tag").getValue().toString() + " added new FoodShare item!");
                        addedFoodshareHolder.date.setText(addedFoodShareNotification.getDate());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                addedFoodshareHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(holder.getAdapterPosition());
                        mUsersDatabaseReference.child(userDotlessEmail).child("notifications").child(addedFoodShareNotification.getKey()).removeValue();
                    }
                });
                break;
            case ListNotifications.TYPE_GROCERY_ADDED_NOTIFICATION:
                final AddedGroceryNotification addedGroceryNotification = (AddedGroceryNotification) notifications.get(position);
                final AddedGroceryHolder addedGroceryHolder = (AddedGroceryHolder) holder;

                mUsersDatabaseReference.child(addedGroceryNotification.getSenderKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        addedGroceryHolder.title.setText("GROCERY");
                        addedGroceryHolder.message.setText("User " + dataSnapshot.child("tag").getValue().toString() + " added new Grocery item!");
                        addedGroceryHolder.date.setText(addedGroceryNotification.getDate());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                addedGroceryHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(holder.getAdapterPosition());
                        mUsersDatabaseReference.child(userDotlessEmail).child("notifications").child(addedGroceryNotification.getKey()).removeValue();
                    }
                });
                break;
            case ListNotifications.TYPE_CHORE_ADDED_NOTIFICATION:
                final AddedChoreNotification addedChoreNotification = (AddedChoreNotification) notifications.get(position);
                final AddedChoreHolder addedChoreHolder = (AddedChoreHolder) holder;

                mUsersDatabaseReference.child(addedChoreNotification.getSenderKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        addedChoreHolder.title.setText("CHORES");
                        addedChoreHolder.message.setText("User " + dataSnapshot.child("tag").getValue().toString() + " added new Chore!");
                        addedChoreHolder.date.setText(addedChoreNotification.getDate());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                addedChoreHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(holder.getAdapterPosition());
                        mUsersDatabaseReference.child(userDotlessEmail).child("notifications").child(addedChoreNotification.getKey()).removeValue();
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return notifications.get(position).getListNotificationType();
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @Override
    public void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void initializeFirebaseDatabaseReferences(String dotlessEmail) {
        mSentNotificationsDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("sentNotifications");
        mReceivedNotificationsDatabaseReference = mFirebaseDatabase.getReference().child("notifications").child("receivedNotifications");
        mUserFlatsDatabaseReference = mFirebaseDatabase.getReference().child("userFlats");
        mFlatUsersDatabaseReference = mFirebaseDatabase.getReference().child("flatUsers");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }
}
