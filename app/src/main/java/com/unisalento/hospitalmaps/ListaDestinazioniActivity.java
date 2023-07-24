package com.unisalento.hospitalmaps;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListaDestinazioniActivity extends AppCompatActivity {
    private AutoCompleteTextView cercaReparto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinazioni);
        cercaReparto = findViewById(R.id.cercaReparto);

        List<String> opzioni = new ArrayList<>(); // Popola questa lista con le opzioni ottenute dall'API
        DropDownAdapterActivity adapter = new DropDownAdapterActivity(this, opzioni);

        cercaReparto.setAdapter(adapter);
        // Recupera i dati
        String responseData = getIntent().getStringExtra("risposta_destinazioni");

        try {
            // Analizza i dati JSON dalla risposta del server
            JSONArray destinazioniArray = new JSONArray(responseData);

            // Trova la Tabella in layout
            TableLayout tableLayoutListaDestinazioni = findViewById(R.id.tabellaDestinazioni);

            // Crea righe per ciascun elemento nel JSONArray
            for (int i = 0; i < destinazioniArray.length(); i++) {
                JSONObject destinazione = destinazioniArray.getJSONObject(i);

                String stanza = destinazione.getString("Stanza");
                String numeroStanza = destinazione.getString("Numero stanza");
                String reparto = destinazione.getString("Reparto");

                // Crea una nuova riga
                TableRow tableRow = new TableRow(this);

                // Crea le TextView
                TextView textViewNomeStanza = new TextView(this);
                textViewNomeStanza.setText(stanza);

                TextView textViewNumeroStanza = new TextView(this);
                textViewNomeStanza.setText(numeroStanza);

                TextView textViewReparto = new TextView(this);
                textViewNomeStanza.setText(reparto);

                // Aggiungi le TextView alla riga
                tableRow.addView(textViewNomeStanza);
                tableRow.addView(textViewNumeroStanza);
                tableRow.addView(textViewReparto);

                // Aggiungi la riga alla tabella
                tableLayoutListaDestinazioni.addView(tableRow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}



