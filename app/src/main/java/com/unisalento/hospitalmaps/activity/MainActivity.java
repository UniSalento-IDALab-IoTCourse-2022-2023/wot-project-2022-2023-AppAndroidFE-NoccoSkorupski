package com.unisalento.hospitalmaps.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.Switch;
import android.widget.Toast;

import com.unisalento.hospitalmaps.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button inizia = findViewById(R.id.inizia);
        inizia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPercorsoActivity(view);

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                Switch percorsoSwitch = findViewById(R.id.percorsoAccessibile);
                boolean isPercorsoAccessibile = percorsoSwitch.isChecked();

                editor.putBoolean("isPercorsoAccessibile", isPercorsoAccessibile);

                editor.apply();


            }
        });
    }
    public void openPercorsoActivity(View view) {
        Intent intent = new Intent(this, PercorsoActivity.class);
        startActivity(intent);
    }

    private void getListaDestinazioni() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8081/api/listaDestinazioni";

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Esegui la richiesta in modo asincrono
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Gestione degli errori in caso di fallimento della richiesta
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Errore nella richiesta", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Gestione della risposta ricevuta dal server
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, ListaDestinazioniActivity.class);
                        intent.putExtra("risposta_destinazioni", responseData);
                        startActivity(intent);
                    }
                });
            }
        });
    }







}