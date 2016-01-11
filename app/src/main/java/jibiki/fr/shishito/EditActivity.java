package jibiki.fr.shishito;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.EditText;

import jibiki.fr.shishito.Models.ListEntry;

public class EditActivity extends BaseActivity {

    private ListEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        entry = (ListEntry) intent.getSerializableExtra(SearchActivity.ENTRY);

        EditText editText = (EditText)findViewById(R.id.kanji);
        editText.setText(entry.getKanji());
        editText = (EditText)findViewById(R.id.hiragana);
        editText.setText(entry.getHiragana());
        editText = (EditText)findViewById(R.id.romaji);
        editText.setText(entry.getRomanji());
        editText = (EditText)findViewById(R.id.gram);
        editText.setText(entry.getGram());

    }
}
