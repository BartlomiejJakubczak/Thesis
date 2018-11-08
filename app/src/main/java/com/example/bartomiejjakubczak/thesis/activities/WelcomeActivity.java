package com.example.bartomiejjakubczak.thesis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bartomiejjakubczak.thesis.R;

public class WelcomeActivity extends AppCompatActivity {

    private TextView welcome_label;
    private TextView explanation;
    private TextView explanation2;
    private Button joinFlat;
    private Button createFlat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        welcome_label = findViewById(R.id.welcome_label);
        explanation = findViewById(R.id.explanation);
        explanation2 = findViewById(R.id.explanation2);
        joinFlat = findViewById(R.id.join_flat_button);
        createFlat = findViewById(R.id.create_flat_button);

        joinFlat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        createFlat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateFlatActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }



}
