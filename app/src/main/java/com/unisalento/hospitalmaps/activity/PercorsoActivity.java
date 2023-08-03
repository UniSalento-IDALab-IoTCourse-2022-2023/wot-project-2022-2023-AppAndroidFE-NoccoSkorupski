package com.unisalento.hospitalmaps.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.unisalento.hospitalmaps.R;

import java.util.List;

public class PercorsoActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor magnetometerSensor;
    private Sensor accelerometerSensor;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[3];
    private float[] orientation = new float[3];

    boolean isLastAccelerometerArrayCopied=false;
    boolean isLastMagnetometerArrayCopied=false;

    long lastUpdateTime=0;
    float currentDegree=0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percorso);

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, magnetometerSensor);
        sensorManager.unregisterListener(this, accelerometerSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(sensorEvent.sensor == accelerometerSensor) {
            System.arraycopy(sensorEvent.values, 0, lastAccelerometer,0, sensorEvent.values.length);
            isLastAccelerometerArrayCopied=true;
        }else if(sensorEvent.sensor == magnetometerSensor) {
            System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;
        }
        if(isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdateTime>250){
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuthInRadians = orientation[0];
            float azimuthInDegree = (float) Math.toDegrees(azimuthInRadians);
            Toast.makeText(this, Float.toString(azimuthInDegree)+"°", Toast.LENGTH_SHORT).show();

            currentDegree=-azimuthInDegree;
            lastUpdateTime=System.currentTimeMillis();

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non viene utilizzato in questo esempio
    }
}


