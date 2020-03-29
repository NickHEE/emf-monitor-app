// icons: C:\Users\Joseph\AppData\Local\Android\Sdk\platforms\android-29\data\res\drawable-hdpi


package com.example.emf_monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static final int REQUEST_SETTINGS = 1;

    // Random magnetic values
    private boolean DEBUG = true;

    // delay between EMF samples (in microseconds...check to see how accurate this is though)
    // otherwise need to use constant like: SensorManager.SENSOR_DELAY_FASTEST)
    private int EMF_delay = 50000;

    // setup/management variables for magnetometer
    private Sensor magnetometer;
    private SensorManager magSensorManager;
    private String TAG = "magnetometer ";

    // Variables for holding magnetometer readings
    private LineGraphSeries<DataPoint> mGraphData;
    private LineGraphSeries<DataPoint> mAlarmData;
    private ArrayList<Double> mRecordedData = new ArrayList<Double>();
    private float[] mData = null;
    private double mMax = 0.0;

    ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    private int alarm_threshold = 100;

    private String EMF_reading_string = "Max:  %.1f %s\nAvg:  %.1f %s";
    private double graphLastXValue = 0;

    private boolean is_mG = true;
    private boolean is_recording = false;
    private boolean is_tone_playing = false;

    private Chronometer chronometer;
    private TextView EMF_reading;
    private GraphView graph;
    private Button recordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        magSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = magSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        EMF_reading = (TextView) findViewById(R.id.EMF_field);
        graph = (GraphView) findViewById(R.id.graph);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        recordButton = (Button) findViewById(R.id.recordButton);

        // see if there is a magnetometer
        if (magnetometer == null){
            finish();
        }

        initGraph();
        if (!is_recording) {
            EMF_reading.setVisibility(View.INVISIBLE);
            chronometer.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            Log.d("onActivityResult", "received alarm value");
            Bundle args =  data.getExtras();
            is_mG = args.getBoolean("UNITS", false);
            alarm_threshold = args.getInt("THRESHOLD",0);
        }
    }

    // method for setting a threshold for the alarm
    public void onSettings(final View v) {

        Intent i = new Intent(this, AlarmActivity.class);
        i.putExtra("CURRENT_THRESHOLD", alarm_threshold);
        i.putExtra("CURRENT_UNITS", is_mG);
        startActivityForResult(i, REQUEST_SETTINGS);
    }

    public void onRecord(View v) {
        if (!is_recording) {

            //Get date and do any initializations for db
            // ...

            EMF_reading.setVisibility(View.VISIBLE);
            EMF_reading.setText(String.format(EMF_reading_string, 0.0, getUnit(), 0.0, getUnit()));
            chronometer.setVisibility(View.VISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            is_recording = true;
            recordButton.setText("Stop");
            initGraph();
        }
        else {

            EMF_reading.setVisibility(View.INVISIBLE);
            chronometer.setVisibility(View.INVISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            is_recording = false;
            recordButton.setText("Record");

            // Do DB stuff
            // ...

            initGraph();
            mMax = 0;

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
            mData = new float[3];
            System.arraycopy(event.values, 0, mData, 0, 3);
        }

        // display magnetometer information
        if (mData != null) {
            //EMF_reading_string = "m_x : " + mData[0] + " m_y : " + mData[1] + " m_z : " + mData[2];
            //Log.d(TAG, "m_x : " + mData[0] + " m_y : " + mData[1] + " m_z : " + mData[2]);

            double mRMS = Math.sqrt(Math.pow(mData[0], 2) + Math.pow(mData[1], 2) + Math.pow(mData[2], 2));
            if (DEBUG) {
                Random rand = new Random();
                mRMS = rand.nextDouble() * 115;
            }
            mRecordedData.add(mRMS);

            if (mRMS > mMax) {
                mMax = mRMS;
                if (!is_tone_playing){
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2);
                    is_tone_playing = true;
                }

            }
            else {
                if (is_tone_playing) {
                    toneGen.stopTone();
                    is_tone_playing = false;
                }
            }

            double sum = 0.0;
            double avg;
            for (double m : mRecordedData) {
                sum += m;
            }
            avg = sum / mRecordedData.size();

            String unit = is_mG ? "mG" : "uT";
            EMF_reading.setText(String.format(EMF_reading_string, mMax, getUnit(), avg, getUnit()));

            graphLastXValue += 0.05d;
            mGraphData.appendData(new DataPoint(graphLastXValue, mRMS), true, 300);
            mAlarmData.appendData(new DataPoint(graphLastXValue, alarm_threshold), true, 300);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // N/A
    }

    public String getUnit() {
        return is_mG ? "mG" : "uT";
    }

    public void initGraph() {

        graph.removeAllSeries();
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(12);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(200);

        mGraphData = new LineGraphSeries<>();
        mGraphData.setTitle("RMS Magnetic Field");
        mGraphData.setDrawDataPoints(false);
        mGraphData.setDrawBackground(true);

        mAlarmData = new LineGraphSeries<>();
        mAlarmData.setTitle("Alarm Threshold");
        mAlarmData.setDrawDataPoints(false);
        mAlarmData.setDrawBackground(false);
        mAlarmData.setDrawAsPath(true);
        mAlarmData.setColor(Color.RED);
        Paint alarmPaint = new Paint();
        alarmPaint.setStyle(Paint.Style.STROKE);
        alarmPaint.setStrokeWidth(10);
        alarmPaint.setPathEffect(new DashPathEffect(new float[] {1, 0.625f}, 0));
        alarmPaint.setColor(Color.RED);
        mAlarmData.setCustomPaint(alarmPaint);

        graph.addSeries(mGraphData);
        graph.addSeries(mAlarmData);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphLastXValue = 0;

    }

}
