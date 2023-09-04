package com.unisalento.hospitalmaps.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.unisalento.hospitalmaps.model.Mappa;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PercorsoActivity extends AppCompatActivity  implements SensorEventListener, BeaconConsumer{

    private SensorManager sensorManager;
    private Sensor magnetometro;
    private TextView txtAzimuth;
    private Handler handler;
    private Runnable runnable;
    List<Mappa> mappaItems = new ArrayList<>();
    List<Mappa> mappaItems1 = new ArrayList<>();

    final String CODICE_OSPEDALE = "01";
    private String reparto;
    private String stanza;
    private Integer gradiPartenza;
    private Integer gradiPartenza1;

    private BeaconManager beaconManager;
    private ArrayList<Beacon> beacons = new ArrayList<>();
    Collection<Beacon> beaconsInRange;
    private String nuovoNearestBeaconUuid;
    private boolean isScanning = false;
    private String nearestBeaconUuid;
    private boolean presente = false;
    private boolean presente1=false;


    private Integer posizioneRicomincia1;
    private boolean none = false;

    private float azimuth;

    private ImageView destraImage;
    private TextView destraText;
    private ImageView sinistraImage;
    private TextView sinistraText;
    private ImageView drittoImage;
    private TextView drittoText;
    private ImageView indietroImage;
    private TextView indietroText;
    private ImageView drittodestraImage;
    private TextView drittodestraText;
    private ImageView drittosinistrsImage;
    private TextView drittosinistraText;
    private ImageView saliscaleImage;
    private TextView saliscaleText;
    private ImageView scendiscaleImage;
    private TextView scendisaleText;
    private ImageView scaleaccessibiliImage;
    private TextView scaleaccessibiliText;
    private ImageView progressImage;
    private TextView progressText;
    private ImageView arrivatoImage;
    private TextView arrivatoText;
    private JSONArray mappaArray;
    private final Object mappaItemsLock = new Object();

    private boolean destra=false;
    private boolean sinistra=false;
    private boolean drittodestra=false;
    private boolean drittosinistra=false;
    private boolean dritto=false;
    private boolean indietro=false;
    private boolean saliscale=false;
    private boolean scendiscale=false;
    private boolean saliscaleaccessiili=false;
    private boolean scendiscaleaccessiili=false;
    private boolean progress=false;
    private boolean arrivato=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percorso);
        destraImage = findViewById(R.id.destraImage1);
        destraText = findViewById(R.id.destraText1);
        sinistraImage = findViewById(R.id.sinistraImage);
        sinistraText = findViewById(R.id.sinistraText);
        drittodestraImage = findViewById(R.id.drittodestraImage);
        drittodestraText = findViewById(R.id.drittodestraText);
        drittosinistrsImage = findViewById(R.id.drittosinistraImage);
        drittosinistraText = findViewById(R.id.drittosinistraText);
        drittoImage = findViewById(R.id.drittoImage);
        drittoText = findViewById(R.id.drittoText);
        indietroImage = findViewById(R.id.indietroImage);
        indietroText = findViewById(R.id.indietroText);
        saliscaleImage = findViewById(R.id.saliscaleImage);
        saliscaleText = findViewById(R.id.saliscaleText);
        scendiscaleImage = findViewById(R.id.scendiscaleImage);
        scendisaleText = findViewById(R.id.scendiscaleText);
        scaleaccessibiliImage = findViewById(R.id.scaleaccessibiliImage);
        scaleaccessibiliText = findViewById(R.id.scaleaccessibiliText);
        progressImage = findViewById(R.id.progressImage);
        progressText = findViewById(R.id.progressText);
        arrivatoImage = findViewById(R.id.arrivatoImage);
        arrivatoText = findViewById(R.id.arrivatoText);
        progress = true;
        updateViewsVisibility();


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Requisisci il permesso
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
        } else {
            // Richiedi la posizione corrente
            sensorManager.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_NORMAL);
        }



        Intent intent = getIntent();
        nearestBeaconUuid = intent.getStringExtra("NEARESTUUID");
        reparto = intent.getStringExtra("REPARTO");
        stanza = intent.getStringExtra("STANZA");
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean percorsoAccessibile = sharedPreferences.getBoolean("isPercorsoAccessibile", false);

        // Verifica e richiedi le autorizzazioni necessarie
        checkPermissions();

        // Inizializza il BeaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        if(!percorsoAccessibile){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("uuidPartenza", nearestBeaconUuid);
                jsonObject.put("repartoArrivo", reparto);
                jsonObject.put("stanzaArrivo", stanza);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new PercorsoActivity.FetchDataTask().execute(jsonObject.toString());

            while(mappaItems.isEmpty() || mappaArray.length() != mappaItems.size()){
            }
            indicazioni(nearestBeaconUuid, mappaItems);
            controlli();
        }
        else{
            JSONObject jsonObject1 = new JSONObject();
            try {
                jsonObject1.put("uuidPartenza", nearestBeaconUuid);
                jsonObject1.put("repartoArrivo", reparto);
                jsonObject1.put("stanzaArrivo", stanza);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new PercorsoActivity.FetchDataTask1().execute(jsonObject1.toString());
            while(mappaItems1.isEmpty() || mappaArray.length() != mappaItems1.size()){
            }
            indicazioni1(nearestBeaconUuid, mappaItems1);
            controlli1();
        }



    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Ottieni il campo magnetico corrente
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calcola l'azimut
        azimuth = (float) Math.atan2(y, x);

        // Controlla se l'azimut è negativo
        if (azimuth < 0) {
            // Se l'azimut è negativo, aggiungi 2 * Math.PI ad esso
            azimuth += 2 * Math.PI;
        }

        // Converti l'azimut in gradi
        azimuth = (float) (azimuth * 180 / Math.PI);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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


        beaconsInRange = new ArrayList<>();
        // Assicurati di aver richiesto i permessi necessari nel file Manifest
        beaconsInRange.addAll(beacons);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beaconsInRange, Region region) {
                if (!isScanning) {
                    isScanning = false;
                    if (!beaconsInRange.isEmpty()) {
                        beacons.clear(); // Rimuovi i beacon precedenti dalla lista
                        beacons.addAll(beaconsInRange); // Aggiungi i nuovi beacon rilevati alla lista

                        // Assicurati che ci siano elementi nella lista 'beacons'
                        if (!beacons.isEmpty()) {
                            Beacon nearestBeacon = beacons.get(0); // Ottieni il primo elemento della lista
                            nuovoNearestBeaconUuid = nearestBeacon.getId1().toString();
                            // Ora hai l'UUID del beacon più vicino
                            //Toast.makeText(PercorsoActivity.this, "Nuovo beacon più vicino UUID: " + nuovoNearestBeaconUuid, Toast.LENGTH_LONG).show();
                        } else {
                            Log.i("Beacon Data", "Nessun beacon trovato.");
                            nuovoNearestBeaconUuid = null;
                        }
                        isScanning = false;
                    }
                }
            }
        });
        beaconManager.setForegroundBetweenScanPeriod(1500); // Rallenta la scansione ogni 5 secondi
        beaconManager.setForegroundScanPeriod(1500);

        try {
            // Avvia la scansione dei beacon
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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




    private class FetchDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "http://ec2-52-22-228-41.compute-1.amazonaws.com:8081/api/utente/ottieniPercorso/"+CODICE_OSPEDALE;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = null;

            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // La richiesta è andata a buon fine
                    String responseString = response.body().string();
                    if (responseString != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseString);
                            gradiPartenza = jsonResponse.getInt("gradiPartenza");
                            mappaArray = jsonResponse.getJSONArray("mappa");
                            for (int i = 0; i < mappaArray.length(); i++) {
                                JSONObject mappaObject = mappaArray.getJSONObject(i);

                                // Creare un oggetto MappaItem e impostare le proprietà
                                Mappa item = new Mappa();
                                item.setBeaconUUID(mappaObject.getString("beaconUUID"));
                                item.setNord(mappaObject.getString("nord"));
                                item.setSud(mappaObject.getString("sud"));
                                item.setEst(mappaObject.getString("est"));
                                item.setOvest(mappaObject.getString("ovest"));
                                item.setPosizione(mappaObject.getInt("posizione"));

                                // Aggiungere l'oggetto MappaItem alla lista
                                mappaItems.add(item);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Gestisci il caso in cui la chiamata di rete ha restituito un errore
                        Log.e("API_ERROR", "Errore nella chiamata API");
                    }
                    return responseString;
                } else {
                    // La richiesta ha restituito un errore
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }




    }

    private class FetchDataTask1 extends AsyncTask<String, Void, String>  {
        @Override
        protected String doInBackground(String... params) {
            String url = "http://172.20.10.5:8081/api/utente/ottieniPercorsoDisabili/"+CODICE_OSPEDALE;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = null;

            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // La richiesta è andata a buon fine
                    String responseString = response.body().string();
                    if (responseString != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseString);
                            gradiPartenza = jsonResponse.getInt("gradiPartenza");
                            mappaArray = jsonResponse.getJSONArray("mappa");
                            for (int i = 0; i < mappaArray.length(); i++) {
                                JSONObject mappaObject = mappaArray.getJSONObject(i);

                                // Creare un oggetto MappaItem e impostare le proprietà
                                Mappa item = new Mappa();
                                item.setBeaconUUID(mappaObject.getString("beaconUUID"));
                                item.setNord(mappaObject.getString("nord"));
                                item.setSud(mappaObject.getString("sud"));
                                item.setEst(mappaObject.getString("est"));
                                item.setOvest(mappaObject.getString("ovest"));
                                item.setPosizione(mappaObject.getInt("posizione"));

                                // Aggiungere l'oggetto MappaItem alla lista
                                mappaItems1.add(item);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Gestisci il caso in cui la chiamata di rete ha restituito un errore
                        Log.e("API_ERROR", "Errore nella chiamata API");
                    }
                    return responseString;
                } else {
                    // La richiesta ha restituito un errore
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


        }
    }
    public void controlli(){
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                indicazioni(nearestBeaconUuid, mappaItems);
                    if(nuovoNearestBeaconUuid != null && !nuovoNearestBeaconUuid.equals(nearestBeaconUuid)) {
                        nearestBeaconUuid = nuovoNearestBeaconUuid;
                        presente = false;
                        synchronized (mappaItemsLock) {
                        for (Mappa mappa : mappaItems) {
                            if (mappa.getBeaconUUID().equals(nearestBeaconUuid)) {
                                presente = true;
                                indicazioni(nearestBeaconUuid, mappaItems);
                                break;
                            }
                        }
                        if (!presente) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("uuidPartenza", nuovoNearestBeaconUuid);
                                jsonObject.put("repartoArrivo", reparto);
                                jsonObject.put("stanzaArrivo", stanza);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mappaItems.clear();
                            new PercorsoActivity.FetchDataTask().execute(jsonObject.toString());
                        }
                    }
                }
                handler.postDelayed(this, 2000); // Esegui nuovamente il Runnable dopo 1 secondo
            }
        };

        // Avvia il Runnable per la prima volta
        handler.postDelayed(runnable, 5000);
}

    public void controlli1(){
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                indicazioni1(nearestBeaconUuid, mappaItems1);
                if(nuovoNearestBeaconUuid != null && !nuovoNearestBeaconUuid.equals(nearestBeaconUuid)) {
                    nearestBeaconUuid = nuovoNearestBeaconUuid;
                    presente = false;
                    synchronized (mappaItemsLock) {
                        for (Mappa mappa : mappaItems1) {
                            if (mappa.getBeaconUUID().equals(nearestBeaconUuid)) {
                                presente = true;
                                indicazioni1(nearestBeaconUuid, mappaItems1);
                                break;
                            }
                        }
                        if (!presente) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("uuidPartenza", nuovoNearestBeaconUuid);
                                jsonObject.put("repartoArrivo", reparto);
                                jsonObject.put("stanzaArrivo", stanza);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mappaItems1.clear();
                            new PercorsoActivity.FetchDataTask1().execute(jsonObject.toString());
                        }
                    }
                }
                handler.postDelayed(this, 2000); // Esegui nuovamente il Runnable dopo 1 secondo
            }
        };

        // Avvia il Runnable per la prima volta
        handler.postDelayed(runnable, 5000);

    }

    private void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    private void indicazioni(String beaconUuid, List<Mappa> mappaArray){
        String direzione="";
        for(int i=0; i < mappaArray.size(); i++) {

            if(mappaArray.get(i).getBeaconUUID().equals(beaconUuid)) {
                float gradi = azimuth - gradiPartenza;
                if (gradi < 0)
                    gradi = 360 + gradi;
                if ((gradi >= 0 && gradi < 45) || (gradi >= 315 && gradi <= 360))
                    direzione = "nord";
                else if (gradi >= 45 && gradi < 135)
                    direzione = "est";
                else if (gradi >= 135 && gradi < 225)
                    direzione = "sud";
                else
                    direzione = "ovest";

                if (direzione.equals("nord")) {
                    if (mappaArray.get(i).getNord().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("SALI_SCALE")) {
                        saliscale = true;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("SCENDI_SCALE")) {
                        scendiscale = true;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                if (direzione.equals("sud")) {
                    if (mappaArray.get(i).getSud().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("SALI_SCALE")) {
                        saliscale = true;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("SCENDI_SCALE")) {
                        scendiscale = true;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                if (direzione.equals("est")) {
                    if (mappaArray.get(i).getEst().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("SALI_SCALE")) {
                        saliscale = true;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("SCENDI_SCALE")) {
                        scendiscale = true;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                if (direzione.equals("ovest")) {
                    if (mappaArray.get(i).getOvest().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("SALI_SCALE")) {
                        saliscale = true;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("SCENDI_SCALE")) {
                        scendiscale = true;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("ARRIVATO")) {
                        arrivato = true;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                break;
            }
        }

    }
    private void indicazioni1(String beaconUuid, List<Mappa> mappaArray){
        String direzione="";
        for(int i=0; i < mappaArray.size(); i++) {

            if(mappaArray.get(i).getBeaconUUID().equals(beaconUuid)) {
                float gradi = azimuth - gradiPartenza;
                if (gradi < 0)
                    gradi = 360 + gradi;
                if ((gradi >= 0 && gradi < 45) || (gradi >= 315 && gradi <= 360))
                    direzione = "nord";
                else if (gradi >= 45 && gradi < 135)
                    direzione = "est";
                else if (gradi >= 135 && gradi < 225)
                    direzione = "sud";
                else
                    direzione = "ovest";

                if (direzione.equals("nord")) {
                    if (mappaArray.get(i).getNord().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("SALI_SCALE")) {
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = true;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("SCENDI_SCALE")) {
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = true;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getNord().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                if (direzione.equals("sud")) {
                    if (mappaArray.get(i).getSud().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("SALI_SCALE")) {
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = true;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("SCENDI_SCALE")) {
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscaleaccessiili = true;
                        saliscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getSud().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                if (direzione.equals("est")) {
                    if (mappaArray.get(i).getEst().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("SALI_SCALE")) {
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = true;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("SCENDI_SCALE")) {
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = true;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getEst().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                if (direzione.equals("ovest")) {
                    if (mappaArray.get(i).getOvest().equals("DESTRA")) {
                        destra = true;
                        sinistra = false;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("SINISTRA")) {
                        sinistra = true;
                        dritto = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DRITTO")) {
                        dritto = true;
                        sinistra = false;
                        indietro = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DIETRO")) {
                        indietro = true;
                        dritto = false;
                        sinistra = false;
                        drittodestra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DRITTO_DESTRA")) {
                        drittodestra = true;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        drittosinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("DRITTO_SINISTRA")) {
                        drittosinistra = true;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscale = false;
                        scendiscale = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("SALI_SCALE")) {
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        scendiscale = false;
                        saliscaleaccessiili = true;
                        scendiscaleaccessiili = false;
                        destra = false;
                        arrivato = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("SCENDI_SCALE")) {
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = true;
                        arrivato = false;
                        destra = false;
                        progress = false;
                        updateViewsVisibility();
                    } else if (mappaArray.get(i).getOvest().equals("ARRIVATO")) {
                        saliscaleaccessiili = false;
                        scendiscaleaccessiili = false;
                        scendiscale = false;
                        saliscale = false;
                        drittosinistra = false;
                        drittodestra = false;
                        indietro = false;
                        dritto = false;
                        sinistra = false;
                        destra = false;
                        arrivato = true;
                        progress = false;
                        updateViewsVisibility();
                    }
                }
                break;
            }
        }

    }

    private void updateViewsVisibility() {
        if (destra) {
            destraImage.setVisibility(View.VISIBLE);
            destraText.setVisibility(View.VISIBLE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(sinistra){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.VISIBLE);
            sinistraText.setVisibility(View.VISIBLE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(dritto){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.VISIBLE);
            drittoText.setVisibility(View.VISIBLE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(indietro){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.VISIBLE);
            indietroText.setVisibility(View.VISIBLE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(drittodestra){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.VISIBLE);
            drittodestraText.setVisibility(View.VISIBLE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(drittosinistra){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.VISIBLE);
            drittosinistraText.setVisibility(View.VISIBLE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(saliscale){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.VISIBLE);
            saliscaleText.setVisibility(View.VISIBLE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(scendiscale){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.VISIBLE);
            scendisaleText.setVisibility(View.VISIBLE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(saliscaleaccessiili){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.VISIBLE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.VISIBLE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(scendiscaleaccessiili){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.VISIBLE);
            scaleaccessibiliImage.setVisibility(View.VISIBLE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(none){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }else if(progress){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            arrivatoImage.setVisibility(View.GONE);
            arrivatoText.setVisibility(View.GONE);
        }
        else if(arrivato){
            destraImage.setVisibility(View.GONE);
            destraText.setVisibility(View.GONE);
            sinistraImage.setVisibility(View.GONE);
            sinistraText.setVisibility(View.GONE);
            drittoImage.setVisibility(View.GONE);
            drittoText.setVisibility(View.GONE);
            indietroImage.setVisibility(View.GONE);
            indietroText.setVisibility(View.GONE);
            drittodestraImage.setVisibility(View.GONE);
            drittodestraText.setVisibility(View.GONE);
            drittosinistrsImage.setVisibility(View.GONE);
            drittosinistraText.setVisibility(View.GONE);
            saliscaleImage.setVisibility(View.GONE);
            saliscaleText.setVisibility(View.GONE);
            scendiscaleImage.setVisibility(View.GONE);
            scendisaleText.setVisibility(View.GONE);
            scaleaccessibiliImage.setVisibility(View.GONE);
            scaleaccessibiliText.setVisibility(View.GONE);
            progressImage.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            arrivatoImage.setVisibility(View.VISIBLE);
            arrivatoText.setVisibility(View.VISIBLE);
        }

    }

}


