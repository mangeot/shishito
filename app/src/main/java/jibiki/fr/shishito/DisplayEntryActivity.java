package jibiki.fr.shishito;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.ListEntry;

public class DisplayEntryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entry);
        Intent intent = getIntent();
        ListEntry entry = (ListEntry) intent.getSerializableExtra(SearchActivity.ENTRY);
        TextView vedette = (TextView) findViewById(R.id.vedette);
        String vText = entry.getKanji() +
                "   [<font color=#cc0000>" + entry.getHiragana() + "</font>]   " +
                "   [<font color=#cc0000>" + entry.getRomanji() + "</font>]";
        vedette.setText(Html.fromHtml(vText));
        TextView definition = (TextView) findViewById(R.id.definition);
        definition.setText(entry.getDefinition());
        TextView gram = (TextView) findViewById(R.id.gram);
        gram.setText(entry.getGram());

      /*  ListView listView = (ListView) findViewById(R.id.examples);
        ArrayList<String> examples = new ArrayList<String>();
        for(int i = 0; i< entry.getExamples().size(); i++){
            Example example = entry.getExamples().get(i);
            examples.add(entry.getExamples().get(i).getKanji() +
                    "   [<font color=#cc0000>" + entry.getExamples().get(i).getHiragana() + "</font>]   " +
                    "   [<font color=#cc0000>" + entry.getExamples().get(i).getRomanji() + "</font>]");
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, examples);
        listView.setAdapter(adapter);*/
    }
}
