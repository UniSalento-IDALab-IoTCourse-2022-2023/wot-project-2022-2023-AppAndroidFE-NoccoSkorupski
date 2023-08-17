package com.unisalento.hospitalmaps.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unisalento.hospitalmaps.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class CercaBeaconVicinoActivity extends  AppCompatActivity implements BeaconConsumer {

    private ImageView imageView;
    private TextView statusTextView;
    private ImageView imageView1;
    private TextView statusTextView1;

    private BeaconManager beaconManager;
    private ArrayList<Beacon> beacons = new ArrayList<>();
    Collection<Beacon> beaconsInRange;

    private boolean isScanning = false;



    private String nearestBeaconUuid = "";

    private boolean isScanningPaused = false;
    private Handler scanningTimeoutHandler = new Handler();
    private boolean shouldShowViews = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cercabeaconvicino);

        // Verifica e richiedi le autorizzazioni necessarie
        checkPermissions();

        // Inizializza il BeaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        statusTextView = findViewById(R.id.statusTextView);
        imageView = findViewById(R.id.imageView);
        statusTextView1 = findViewById(R.id.statusTextView1);
        imageView1 = findViewById(R.id.imageView1);

        updateViewsVisibility();

    }



    private void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Avvia la scansione dei beacon dopo aver ottenuto le autorizzazioni
                startBeaconScanning();
            } else {
                // Gestisci il caso in cui l'utente nega le autorizzazioni
                Toast.makeText(this, "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startBeaconScanning() {

        imageView.setVisibility(View.VISIBLE);
        statusTextView.setVisibility(View.VISIBLE);

        scanningTimeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (nearestBeaconUuid==null) {
                    stopBeaconScanning();
                    shouldShowViews=false;
                    updateViewsVisibility();
                }

            }
        }, 10000);
        beaconsInRange = new ArrayList<>();
        // Assicurati di aver richiesto i permessi necessari nel file Manifest
        beaconsInRange.addAll(beacons);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beaconsInRange, Region region) {
                if (!isScanning) {
                    isScanning = true;
                    if (!isScanningPaused) {
                        if (!beaconsInRange.isEmpty()) {
                            beacons.clear(); // Rimuovi i beacon precedenti dalla lista
                            beacons.addAll(beaconsInRange); // Aggiungi i nuovi beacon rilevati alla lista

                            // Assicurati che ci siano elementi nella lista 'beacons'
                            if (!beacons.isEmpty()) {
                                Beacon nearestBeacon = beacons.get(0); // Ottieni il primo elemento della lista
                                nearestBeaconUuid = nearestBeacon.getId1().toString();


                                // Ora hai l'UUID del beacon più vicino
                                Toast.makeText(CercaBeaconVicinoActivity.this, "Beacon più vicino UUID: " + nearestBeaconUuid, Toast.LENGTH_LONG).show();
                                Intent intent1 = getIntent();
                                String reparto = intent1.getStringExtra("REPARTO");
                                String stanza = intent1.getStringExtra("STANZA");
                                Intent intent = new Intent(CercaBeaconVicinoActivity.this, PercorsoActivity.class);
                                intent.putExtra("NEARESTUUID", nearestBeaconUuid);
                                intent.putExtra("REPARTO", reparto);
                                intent.putExtra("STANZA", stanza);
                                startActivity(intent);

                            }
                        } else {
                            Log.i("Beacon Data", "Nessun beacon trovato.");

                            nearestBeaconUuid = null;

                        }
                        isScanningPaused = true;

                    }
                }
            }
        });
        beaconManager.setForegroundBetweenScanPeriod(5000); // Rallenta la scansione ogni 5 secondi
        beaconManager.setForegroundScanPeriod(5000);

        try {
            // Avvia la scansione dei beacon
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void stopBeaconScanning() {
        scanningTimeoutHandler.removeCallbacksAndMessages(null);
        try {
            beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        isScanning = false;
        isScanningPaused = false; // Riavvia la scansione
        shouldShowViews = false;
        updateViewsVisibility();

    }


    @Override
    public void onBeaconServiceConnect() {
        // Una volta connesso al BeaconManager, avvia la scansione dei beacon
        startBeaconScanning();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Assicurati di liberare le risorse quando l'Activity viene distrutta
        beaconManager.unbind(this);
    }

    private void updateViewsVisibility() {
        if (shouldShowViews) {
            imageView.setVisibility(View.VISIBLE);
            statusTextView.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.GONE);
            statusTextView1.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.GONE);
            statusTextView.setVisibility(View.GONE);
            imageView1.setVisibility(View.VISIBLE);
            statusTextView1.setVisibility(View.VISIBLE);
        }

    }


}
