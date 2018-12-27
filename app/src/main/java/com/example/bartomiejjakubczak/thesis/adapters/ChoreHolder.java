package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class ChoreHolder extends RecyclerView.ViewHolder {

    TextView date;
    TextView name;
    TextView personAssigned;

    public ChoreHolder(View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.chore_date_model);
        personAssigned = itemView.findViewById(R.id.chore_personAssigned_model);
        name = itemView.findViewById(R.id.chore_name_model);
    }

}
