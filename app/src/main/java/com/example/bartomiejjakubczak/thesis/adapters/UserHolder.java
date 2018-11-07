package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class UserHolder extends RecyclerView.ViewHolder {

    TextView userName;
    TextView userSurname;
    TextView userTag;
    ImageButton deleteUserButton;

    public UserHolder(View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.userName);
        userSurname = itemView.findViewById(R.id.userSurname);
        userTag = itemView.findViewById(R.id.userTag);
        deleteUserButton = itemView.findViewById(R.id.deleteUserButton);
    }
}
