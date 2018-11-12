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
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private TextView welcome_label;
    private TextView explanation;
    private TextView explanation2;
    private Button joinFlat;
    private Button createFlat;
    private Button signOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        welcome_label = findViewById(R.id.welcome_label);
        explanation = findViewById(R.id.explanation);
        explanation2 = findViewById(R.id.explanation2);
        joinFlat = findViewById(R.id.join_flat_button);
        createFlat = findViewById(R.id.create_flat_button);
        signOut = findViewById(R.id.signout_out_button_welcome);

        joinFlat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FlatSearchActivity.class);
                finish();
                startActivity(intent);
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
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }



}
