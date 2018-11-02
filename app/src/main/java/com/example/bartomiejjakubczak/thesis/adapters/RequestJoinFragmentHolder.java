package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;
import com.example.bartomiejjakubczak.thesis.interfaces.ListNotifications;

public class RequestJoinFragmentHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView message;
    Button acceptButton;
    Button declineButton;

    public RequestJoinFragmentHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.join_request_title_model);
        message = itemView.findViewById(R.id.join_request_message_model);
        acceptButton = itemView.findViewById(R.id.join_request_accept_button);
        declineButton = itemView.findViewById(R.id.join_request_reject_button);
    }
}
