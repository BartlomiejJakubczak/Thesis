package com.example.bartomiejjakubczak.thesis.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.FirebaseConnection;
import com.example.bartomiejjakubczak.thesis.interfaces.ListNotifications;
import com.example.bartomiejjakubczak.thesis.models.RequestJoinNotification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<ListNotifications> notifications = new ArrayList<>();
    private List<ListNotifications> notificationsCopy = new ArrayList<>();

    public RequestsFragmentAdapter(Context context, List<ListNotifications> notifications, List<ListNotifications> notificationsCopy) {
        this.context = context;
        this.notifications.addAll(notifications);
        this.notificationsCopy.addAll(notificationsCopy);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ListNotifications.TYPE_JOIN_NOTIFICATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.join_notification_model, parent, false);
                return new RequestJoinFragmentHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ListNotifications.TYPE_JOIN_NOTIFICATION:
                RequestJoinNotification requestJoinNotification = (RequestJoinNotification) notifications.get(position);
                final RequestJoinFragmentHolder joinFragmentHolder = (RequestJoinFragmentHolder)holder;

                joinFragmentHolder.title.setText("JOIN REQUEST");
                joinFragmentHolder.message.setText("User " + requestJoinNotification.getPersonInvolvedTag() + " wants to join your flat "
                + requestJoinNotification.getFlatInvolvedName() + ".");
                joinFragmentHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                joinFragmentHolder.declineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
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
}
