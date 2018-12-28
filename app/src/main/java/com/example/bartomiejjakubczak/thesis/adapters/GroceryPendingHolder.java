package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class GroceryPendingHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView date;
    ImageButton bought;

    public GroceryPendingHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.model_grocery_name);
        date = itemView.findViewById(R.id.model_grocery_date);
        bought = itemView.findViewById(R.id.add_grocery_button);
    }

}
