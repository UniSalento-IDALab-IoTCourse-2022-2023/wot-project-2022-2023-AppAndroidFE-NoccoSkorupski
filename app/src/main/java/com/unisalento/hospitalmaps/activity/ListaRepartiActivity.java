package com.unisalento.hospitalmaps.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unisalento.hospitalmaps.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListaRepartiActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    final String CODICE_OSPEDALE = "01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listareparti);

        tableLayout = findViewById(R.id.tableLayout);
        new FetchDataTask().execute();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            List<String> resultList = new ArrayList<>();
            OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://192.168.1.140:8081/api/utente/reparti/" + CODICE_OSPEDALE)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String col1Value = jsonObject.getString("nomeReparto");

                        // Aggiungi una nuova riga alla tabella per ogni risultato della chiamata API
                        TableRow tableRow = new TableRow(ListaRepartiActivity.this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ));

                        TextView col1TextView = new TextView(ListaRepartiActivity.this);
                        col1TextView.setText(col1Value);
                        col1TextView.setPadding(8, 8, 8, 8);
                        tableRow.addView(col1TextView);


                        // Aggiungi un pulsante nella colonna
                        Button button = new Button(ListaRepartiActivity.this);
                        button.setText("VEDI STANZE");
                        String reparto = col1Value;
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ListaRepartiActivity.this, ListaStanzeActivity.class);
                                intent.putExtra("REPARTO", reparto);
                                startActivity(intent);
                            }
                        });
                        tableRow.addView(button);

                        View separator = new View(ListaRepartiActivity.this);
                        separator.setBackgroundColor(Color.BLACK);
                        separator.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1));
                        tableLayout.addView(separator);

                        tableLayout.addView(tableRow);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
