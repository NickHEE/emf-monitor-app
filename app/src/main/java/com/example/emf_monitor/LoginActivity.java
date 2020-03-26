package com.example.emf_monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // comment/distance search box
        usernameField = (EditText) findViewById(R.id.username_field);
        passwordField = (EditText) findViewById(R.id.password_field);

        /*
        String username_ = getIntent().getStringExtra("CURRENT_USERNAME");
        String password_ = getIntent().getStringExtra("CURRENT_PASSWORD");
        usernameField.setText(username_);
        passwordField.setText(password_);

        */
    }


    // method for logging in (when "login" button is pressed)
    public void login(final View v) {


        // validate login here


        Intent i = new Intent(this, MainActivity.class); // go to main screen activity
        //i.putExtra("USERNAME", usernameField.getText().toString());
        //i.putExtra("PASSWORD", passwordField.getText().toString());
        startActivity(i);
        finish();
    }


}

