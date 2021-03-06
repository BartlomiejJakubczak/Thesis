package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;


public class AddedGroceryHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView message;
    TextView date;
    ImageView deleteButton;

    public AddedGroceryHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.grocery_notification_title_model);
        message = itemView.findViewById(R.id.grocery_notification_message_model);
        date = itemView.findViewById(R.id.grocery_notification_date);
        deleteButton = itemView.findViewById(R.id.grocery_notif_delete_btn);
    }

}
