package jibiki.fr.shishito;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.ViewUtil;

class EntryListAdapter extends ArrayAdapter<ListEntry> {
    private final Activity activity;
    private transient final ArrayList<ListEntry> values;

    @SuppressWarnings("unused")
    private static final String TAG = EntryListAdapter.class.getSimpleName();

    public EntryListAdapter(Activity activity, ArrayList<ListEntry> values) {
        super(activity, R.layout.word_list_element, values);
        this.activity = activity;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        ListEntry entry = values.get(position);
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.word_list_element, parent, false);

            TextView vedette = (TextView) rowView.findViewById(R.id.vedette);
            ViewUtil.addVedette(vedette, entry, activity, false);
            ViewUtil.addVerified(rowView, entry);
            ViewUtil.parseAndAddGramBlocksToView(rowView, entry, activity, false);
        }
        if (position % 2 == 1) {
            rowView.setBackgroundColor(ContextCompat.getColor(activity, R.color.green));
        } else {
            rowView.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_green));
        }

        return rowView;
    }
}
