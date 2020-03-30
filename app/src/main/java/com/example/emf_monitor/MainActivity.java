// icons: C:\Users\Joseph\AppData\Local\Android\Sdk\platforms\android-29\data\res\drawable-hdpi


package com.example.emf_monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static final int REQUEST_SETTINGS = 1;

    // Random magnetic values
    private boolean DEBUG = true;

    // delay between EMF samples (in microseconds...check to see how accurate this is though)
    // otherwise need to use constant like: SensorManager.SENSOR_DELAY_FASTEST)
    private int EMF_delay = 50000;
    private double EMF_sample_rate = 0.05d;

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
    private double alarm_threshold = 100;

    private String EMF_reading_string = "Max:  %.1f %s\nAvg:  %.1f %s";
    private double graphLastXValue = 0;
    private SimpleDateFormat start;
    private SimpleDateFormat end;
    private int UID;

    private boolean is_mG = true;
    private boolean is_recording = false;
    private boolean is_tone_playing = false;

    private Chronometer chronometer;
    private TextView EMF_reading;
    private GraphView graph;
    private Button recordButton;
    private Button settingsButton;

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
        settingsButton = (Button) findViewById(R.id.settingsButton);

        Intent i = getIntent();
        Bundle args = i.getExtras();

        is_mG = args.getString("UNITS", "mG").equals("mG");
        alarm_threshold = args.getDouble("ALARM_THRESHOLD", 100.0);
        UID = args.getInt("UID",0);

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
            alarm_threshold = args.getDouble("ALARM_THRESHOLD",100.0);
            initGraph();
        }
    }

    // method for setting a threshold for the alarm
    public void onSettings(final View v) {

        Intent i = new Intent(this, SettingsActivity.class);
        i.putExtra("CURRENT_ALARM_THRESHOLD", alarm_threshold);
        i.putExtra("CURRENT_UNITS", is_mG);
        i.putExtra("UID", UID);
        startActivityForResult(i, REQUEST_SETTINGS);
    }

    public void onRecord(View v) {
        if (!is_recording) {

            // Start recording
            start = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            EMF_reading.setVisibility(View.VISIBLE);
            EMF_reading.setText(String.format(EMF_reading_string, 0.0, getUnit(), 0.0, getUnit()));
            chronometer.setVisibility(View.VISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            is_recording = true;
            recordButton.setText("Stop");
            settingsButton.setEnabled(false);

            initGraph();
        }
        else {

            // Stop Recording
            end = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            EMF_reading.setVisibility(View.INVISIBLE);
            chronometer.setVisibility(View.INVISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            is_recording = false;
            recordButton.setText("Record");

            String stringData = mRecordedData.stream().map(Object::toString).collect(Collectors.joining(", "));
            Log.d(TAG, stringData);

            // Insert data into DB
            EMFMonitorDbHelper dbHelper = new EMFMonitorDbHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_UID, UID);
            values.put(EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_DATA, stringData);
            values.put(EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_START, start.toString());
            values.put(EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_STOP, start.toString());
            values.put(EMFMonitorDbHelper.DataContract.DataEntry.COLUMN_NAME_UID, end.toString());

            db.insert(EMFMonitorDbHelper.DataContract.DataEntry.TABLE_NAME, null, values);
            dbHelper.close();

            // Reset graph
            settingsButton.setEnabled(true);
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

            double mRMS = Math.sqrt(Math.pow(mData[0], 2) + Math.pow(mData[1], 2) + Math.pow(mData[2], 2));

            // Generate random values for debugging on emulator
            if (DEBUG) {
                Random rand = new Random();
                mRMS = rand.nextDouble() * 115;
            }

            // Unit conversion
            if (!is_mG) {
                mRMS /= 10;
            }
            mRecordedData.add(mRMS);

            // Play an alarm if the RMS value is above the alarm threshold
            if (mRMS > alarm_threshold) {
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

            // Get average EMF for the session
            double sum = 0.0;
            double avg;
            for (double m : mRecordedData) {
                sum += m;
            }
            avg = sum / mRecordedData.size();

            EMF_reading.setText(String.format(EMF_reading_string, mMax, getUnit(), avg, getUnit()));

            graphLastXValue += EMF_sample_rate;
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
        graph.getViewport().setMaxY(alarm_threshold * 1.3);

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
