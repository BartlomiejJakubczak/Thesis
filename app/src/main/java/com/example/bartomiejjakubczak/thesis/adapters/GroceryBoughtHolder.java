package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class GroceryBoughtHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView quantity;
    ImageButton deleteButton;

    public GroceryBoughtHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.model_grocery_bought_name);
        quantity = itemView.findViewById(R.id.model_grocery_bought_quantity);
        deleteButton = itemView.findViewById(R.id.delete_grocery_bought_button);
    }

}
