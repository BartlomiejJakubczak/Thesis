package com.example.bartomiejjakubczak.thesis.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class GroceryCompletedInfoHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView quantity;

    public GroceryCompletedInfoHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.productName);
        quantity = itemView.findViewById(R.id.productQuantity);
    }

}
