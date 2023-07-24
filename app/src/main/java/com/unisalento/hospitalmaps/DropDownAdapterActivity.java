package com.unisalento.hospitalmaps;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class DropDownAdapterActivity extends ArrayAdapter<String> implements Filterable {
    private List<String> originalData;
    private List<String> filteredData;

    public DropDownAdapterActivity(Context context, List<String> data) {
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

                // Esegui la chiamata API per ottenere le opzioni corrispondenti al testo di ricerca (constraint)
                // Aggiorna filteredList con i risultati ottenuti dall'API

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (List<String>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}

