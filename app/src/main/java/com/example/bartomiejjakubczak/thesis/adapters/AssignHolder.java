package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class AssignHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView tag;

    public AssignHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.user_name_model);
        tag = itemView.findViewById(R.id.user_tag_model);
    }
}
