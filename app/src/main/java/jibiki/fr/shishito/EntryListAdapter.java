package jibiki.fr.shishito;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EntryListAdapter extends ArrayAdapter<ListEntry> {
    private final Context context;
    private final ArrayList<ListEntry> values;

    public EntryListAdapter(Context context, ArrayList<ListEntry> values) {
        super(context, R.layout.word_list_element, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.word_list_element, parent, false);
        TextView kanji = (TextView) rowView.findViewById(R.id.kanji);
        TextView hiragana = (TextView) rowView.findViewById(R.id.hiragana);
        TextView romanji = (TextView) rowView.findViewById(R.id.romanji);

        kanji.setText(values.get(position).getKanji());
        hiragana.setText(values.get(position).getHiragana());
        romanji.setText(values.get(position).getRomanji());
        return rowView;
    }
}
