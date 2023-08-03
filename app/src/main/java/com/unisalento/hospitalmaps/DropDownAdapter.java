package com.unisalento.hospitalmaps;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DropDownAdapter extends ArrayAdapter<String> implements Filterable {
    private List<String> originalData;
    private List<String> filteredData;

    public DropDownAdapter(Context context, List<String> data) {
        super(context, android.R.layout.simple_dropdown_item_1line, data);
        this.originalData = data;
        this.filteredData = data;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public String getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredList = new ArrayList<>();

                if (constraint != null) {
                    String query = constraint.toString();

                    // Esegui la chiamata API in modo asincrono
                    new APIOpzioniRepartiCallTask().execute(query);

                    // La lista filtrata verrà aggiornata nel callback onPostExecute dell'AsyncTask
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<String> filteredList = (List<String>) results.values;
                clear(); // Rimuovi tutti gli elementi attualmente nel dropdown
                if (filteredList != null && filteredList.size() > 0) {
                    addAll(filteredList); // Aggiungi gli elementi ottenuti dalla chiamata API al dropdown
                }
                notifyDataSetChanged();
            }
        };
    }
    private class APIOpzioniRepartiCallTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> options = new ArrayList<>();
            String query = params[0];

            // Esegui la chiamata API per ottenere le opzioni corrispondenti al testo di ricerca (query)
            // Utilizza OkHttpClient per effettuare la chiamata
            OkHttpClient client = new OkHttpClient();
            String apiUrl = "http://localhost:8081/api/cercaReparti?query=" + query; // Sostituisci con l'URL della tua API
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // Analizza il corpo della risposta per ottenere le opzioni corrispondenti
                    // In questo esempio, stiamo semplicemente aggiungendo alcune opzioni fittizie alla lista
                    options.add("Opzione 1");
                    options.add("Opzione 2");
                    options.add("Opzione 3");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return options;
        }

        @Override
        protected void onPostExecute(List<String> options) {
            filteredData.clear();
            filteredData.addAll(options);
            notifyDataSetChanged();
        }
    }

}




