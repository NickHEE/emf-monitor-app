package com.example.emf_monitor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {
    private EditText thresholdField;
    private boolean is_mG_units;
    private int threshold_;
    private ToggleButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Intent i = getIntent();

        Bundle args =  i.getExtras();
        is_mG_units = args.getBoolean("CURRENT_UNITS", false);
        threshold_=args.getInt("CURRENT_THRESHOLD",0);

        // keep previously saved threshold value in editText box
        thresholdField = (EditText) findViewById(R.id.threshold_field);
      //  i.getIntExtra("CURRENT_THRESHOLD", threshold_);
        thresholdField.setText(String.valueOf(threshold_));

        // keep previously saved unit value
        btn = (ToggleButton)findViewById(R.id.is_mG);
       // i.getBooleanExtra("CURRENT_UNITS", is_mG_units);
        btn.setChecked(is_mG_units);


        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if ( btn.isChecked()) {
                    // converting uT to mG... 10mG=1uT
                    is_mG_units = true;
                    threshold_ = threshold_*10;
                    thresholdField.setText(String.valueOf(threshold_));
                } else  {
                    is_mG_units = false;
                    threshold_ = threshold_/10;
                    thresholdField.setText(String.valueOf(threshold_));
                }
            }
        });
    }


    // method for cancel button
    public void cancel(final View v) {
        finish();
    }

    // method for applying filter to photo gallery (when "search" button is pressed)
    public void apply(final View v) {

        Intent i = new Intent(); // make intent and load everything onto it
        i.putExtra("THRESHOLD", Integer.valueOf(thresholdField.getText().toString()));
        i.putExtra("UNITS", is_mG_units);
        setResult(RESULT_OK, i);
        finish();
    }

}




