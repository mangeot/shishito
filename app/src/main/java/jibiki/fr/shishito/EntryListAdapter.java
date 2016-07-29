package jibiki.fr.shishito;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.Util.ViewUtil;

public class EntryListAdapter extends ArrayAdapter<ListEntry> {
    private final Context context;
    private final ArrayList<ListEntry> values;
    private Volume volume;
    private boolean isInit = false;

    private static final String TAG = EntryListAdapter.class.getSimpleName();

    public EntryListAdapter(Context context, ArrayList<ListEntry> values, Volume volume) {
        super(context, R.layout.word_list_element, values);
        this.context = context;
        this.values = values;
        this.volume = volume;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        ListEntry entry = values.get(position);
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.word_list_element, parent, false);

            TextView vedette = (TextView) rowView.findViewById(R.id.vedette);
            ViewUtil.addVedette(vedette, entry);

                ViewUtil.addVerified(rowView, entry);
                ViewUtil.addGramBlocksToView(rowView, entry, context, false, volume);
                isInit = true;
        }
        if (position % 2 == 1) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
        } else {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_green));
        }

        return rowView;
    }
}
