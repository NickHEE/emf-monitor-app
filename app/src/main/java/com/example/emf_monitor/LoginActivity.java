package com.example.emf_monitor;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emf_monitor.EMFMonitorDbHelper;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;

    SQLiteDatabase db;
    EMFMonitorDbHelper dbHelper;

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

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }


    // method for logging in (when "login" button is pressed)
    public void login(final View v) {

        // validate login here
        dbHelper = new EMFMonitorDbHelper(this);
        db = dbHelper.getReadableDatabase();

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        Cursor cur = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?",
                                 new String[] {username, password});

        if (cur.moveToFirst()) {
            if (cur.getInt(EMFMonitorDbHelper.CAN_WORK_INDEX) > 0) {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("USERNAME", username);
                i.putExtra("UID", cur.getInt(EMFMonitorDbHelper.UID_INDEX));
                i.putExtra("UNITS", cur.getString(EMFMonitorDbHelper.UNITS_INDEX));
                i.putExtra("ALARM_THRESHOLD", cur.getDouble(EMFMonitorDbHelper.ALARM_THRESHOLD_INDEX));
                startActivity(i);
                finish();
            }
            else {
                Toast toast = Toast.makeText(this, "You have been exposed to unsafe levels of EMF", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else {
            Toast toast = Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT);
            toast.show();
        }


    }


}

