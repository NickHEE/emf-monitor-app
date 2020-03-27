package com.example.emf_monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // delay between EMF samples (in microseconds...check to see how accurate this is though)
    // otherwise need to use constant like: SensorManager.SENSOR_DELAY_FASTEST)
    private int EMF_delay = 50000;

    // setup/management variables for magnetometer
    private Sensor magnetometer;
    private SensorManager magSensorManager;
    private String TAG = "magnetometer ";

    // Variables for holding magnetometer readings
    private float[] magnetometer_data = null;
    private String EMF_reading_string;

    private TextView EMF_reading;
    private GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        magSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = magSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        EMF_reading = (TextView) findViewById(R.id.EMF_field);
        graph = (GraphView) findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        // see if there is a magnetometer
        if (magnetometer != null){
            // Success! There's a magnetometer.
        } else {
            // Failure! No magnetometer.
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register for sensor updates
        magSensorManager.registerListener(this, magnetometer, EMF_delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister all sensors
        magSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // update magnetometer information
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetometer_data = new float[3];
                System.arraycopy(event.values, 0, magnetometer_data, 0, 3);
            }

        // display magnetometer information
        if (magnetometer_data != null) {
            EMF_reading_string = "m_x : " + magnetometer_data[0] + " m_y : " + magnetometer_data[1] + " m_z : " + magnetometer_data[2];
            EMF_reading.setText(EMF_reading_string);
            //Log.d(TAG, "m_x : " + magnetometer_data[0] + " m_y : " + magnetometer_data[1] + " m_z : " + magnetometer_data[2]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // N/A
    }

}
