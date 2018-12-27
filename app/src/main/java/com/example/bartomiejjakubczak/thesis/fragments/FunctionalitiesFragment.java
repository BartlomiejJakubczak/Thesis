package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.bartomiejjakubczak.thesis.R;

public class FunctionalitiesFragment extends Fragment {

    private Button foodShareButton;
    private Button groceryButton;
    private Button choresButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.functionalities_fragment, container, false);
        foodShareButton = view.findViewById(R.id.foodShare_button);
        groceryButton = view.findViewById(R.id.grocery_button);
        choresButton = view.findViewById(R.id.chores_button);
        foodShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FoodShareFragment foodShareFragment = new FoodShareFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, foodShareFragment, "foodShareFragment");
                fragmentTransaction.addToBackStack("functionalitiesFragment");
                fragmentTransaction.commit();
            }
        });
        groceryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroceryPendingFragment groceryPendingFragment = new GroceryPendingFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, groceryPendingFragment, "groceryPendingFragment");
                fragmentTransaction.addToBackStack("functionalitiesFragment");
                fragmentTransaction.commit();
            }
        });
        choresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoresFragment choresFragment = new ChoresFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, choresFragment, "choresFragment");
                fragmentTransaction.addToBackStack("functionalitiesFragment");
                fragmentTransaction.commit();
            }
        });
        return view;
    }


}
