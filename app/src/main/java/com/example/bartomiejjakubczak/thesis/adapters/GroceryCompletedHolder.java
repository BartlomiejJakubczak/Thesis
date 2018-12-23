package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class GroceryCompletedHolder extends RecyclerView.ViewHolder {

    TextView buyer;
    TextView date;

    public GroceryCompletedHolder(View itemView) {
        super(itemView);
        buyer = itemView.findViewById(R.id.model_grocery_completed_buyer);
        date = itemView.findViewById(R.id.model_grocery_completed_date);
    }
}
