package jibiki.fr.shishito;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.ListEntry;

public class DisplayEntryActivity extends BaseActivity {

    private ListEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entry);
        Intent intent = getIntent();
        entry = (ListEntry) intent.getSerializableExtra(SearchActivity.ENTRY);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.add("Edit");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getTitle().equals("Edit")){
            Intent intent = new Intent(DisplayEntryActivity.this, EditActivity.class);
            intent.putExtra(SearchActivity.ENTRY, entry);
            startActivity(intent);
        }
        return true;
    }

}
