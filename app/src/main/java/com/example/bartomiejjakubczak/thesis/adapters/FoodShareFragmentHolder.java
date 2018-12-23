package com.example.bartomiejjakubczak.thesis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

import org.w3c.dom.Text;

public class FoodShareFragmentHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView quantity;

    public FoodShareFragmentHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.model_foodShare_name);
        quantity = itemView.findViewById(R.id.model_foodShare_quantity);
    }
}
