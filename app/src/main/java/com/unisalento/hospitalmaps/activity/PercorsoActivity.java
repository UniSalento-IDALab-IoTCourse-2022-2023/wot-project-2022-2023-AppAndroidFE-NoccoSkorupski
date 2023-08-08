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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.unisalento.hospitalmaps.R;

import java.util.List;

public class PercorsoActivity extends AppCompatActivity  implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor magnetometro;
    private TextView txtAzimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percorso);
        txtAzimuth = findViewById(R.id.txtAzimuth);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Requisisci il permesso
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
        } else {
            // Richiedi la posizione corrente
            sensorManager.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        // Ottieni il campo magnetico corrente
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calcola l'azimut
        float azimuth = (float) Math.atan2(y, x);

        // Controlla se l'azimut è negativo
        if (azimuth < 0) {
            // Se l'azimut è negativo, aggiungi 2 * Math.PI ad esso
            azimuth += 2 * Math.PI;
        }

        // Converti l'azimut in gradi
        azimuth = (float) (azimuth * 180 / Math.PI);

        txtAzimuth.setText("Azimuth: " + azimuth);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}


