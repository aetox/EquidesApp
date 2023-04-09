package com.example.equides;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class InterfaceActivity extends AppCompatActivity {

    private TextView welcomeMailTextView;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        welcomeMailTextView = findViewById(R.id.welcomeMailTextView);

        email = getIntent().getStringExtra("mail");

        welcomeMailTextView.setText(email);

    }
}