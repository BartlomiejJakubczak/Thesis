package com.example.bartomiejjakubczak.thesis.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.bartomiejjakubczak.thesis.R;

public class FunctionalitiesFragment extends Fragment {

    private Button foodShareButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.functionalities_fragment, container, false);
        foodShareButton = view.findViewById(R.id.foodShare_button);
        foodShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // zamiana fragmentu na fragment foodshare z opcja powrotu do maina
            }
        });
        return view;
    }


}