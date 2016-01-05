package jibiki.fr.shishito;


import android.content.Context;
import android.text.Html;
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
        View rowView = convertView;
        if(rowView == null){
            rowView = inflater.inflate(R.layout.word_list_element, parent, false);
        }
        TextView vedette = (TextView) rowView.findViewById(R.id.vedette);
        String vText = values.get(position).getKanji() +
                "   [<font color=#cc0000>" + values.get(position).getHiragana() + "</font>]   " +
                "   [<font color=#cc0000>" + values.get(position).getRomanji() + "</font>]";
        vedette.setText(Html.fromHtml(vText));
        TextView definition = (TextView) rowView.findViewById(R.id.definition);
        definition.setText(values.get(position).getDefinition());

        //romanji.setText(values.get(position).getRomanji());

        if (position % 2 == 1) {
            rowView.setBackgroundColor(rowView.getResources().getColor(R.color.green));
        } else {
            rowView.setBackgroundColor(rowView.getResources().getColor(R.color.light_green));
        }

        return rowView;
    }
}
