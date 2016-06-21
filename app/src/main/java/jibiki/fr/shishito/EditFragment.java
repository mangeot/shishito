package jibiki.fr.shishito;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.HTTPUtils;

public class EditFragment extends Fragment {

    private static final String TAG = EditFragment.class.getSimpleName();

    private static final String ENTRY = "entry";

    private ListEntry entry;
    private Button saveButton;

    public EditFragment() {
    }

    public static EditFragment newInstance(ListEntry entry) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putSerializable(ENTRY, entry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entry = (ListEntry) getArguments().getSerializable(ENTRY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_edit, container, false);
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveButton.setEnabled(true);
            }
        };
        EditText editText = (EditText) v.findViewById(R.id.kanji);
        editText.setText(entry.getKanji());
        editText.addTextChangedListener(tw);
        editText = (EditText) v.findViewById(R.id.hiragana);
        editText.setText(entry.getHiragana());
        editText.addTextChangedListener(tw);
        editText = (EditText) v.findViewById(R.id.romaji);
        editText.setText(entry.getRomanji());
        editText.addTextChangedListener(tw);
        editText = (EditText) v.findViewById(R.id.gram);
        editText.setText(entry.getGram());
        editText.addTextChangedListener(tw);
        editText = (EditText) v.findViewById(R.id.definition);
        editText.setText(entry.getDefinition());
        editText.addTextChangedListener(tw);

        saveButton = (Button) v.findViewById(R.id.button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View but) {
                String xpath;
                String update;
                if (!entry.getKanji().equals(((EditText) v.findViewById(R.id.kanji)).getText().toString())) {
                    xpath = getXPath("cdm-headword");
                    update = ((EditText) v.findViewById(R.id.kanji)).getText().toString();
                } else if (!entry.getHiragana().equals(((EditText) v.findViewById(R.id.hiragana)).getText().toString())) {
                    xpath = getXPath("cdm-reading");
                    update = ((EditText) v.findViewById(R.id.hiragana)).getText().toString();
                } else if (!entry.getRomanji().equals(((EditText) v.findViewById(R.id.romaji)).getText().toString())) {
                    xpath = getXPath("cdm-writing");
                    update = ((EditText) v.findViewById(R.id.romaji)).getText().toString();
                } else if (!entry.getGram().equals(((EditText) v.findViewById(R.id.gram)).getText().toString())) {
                    xpath = getXPath("cdm-definition");
                    update = ((EditText) v.findViewById(R.id.gram)).getText().toString();
                } else {
                    saveButton.setEnabled(false);
                    Toast t = Toast.makeText(getContext(), "Nothing to save", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.TOP, 0, 10);
                    t.show();
                    return;
                }
                //Le xpath n'est pas bon?
                String cdmVolumePath = (String) ((SearchActivity)getActivity()).getVolume().getElements().get("cdm-volume");
                if (xpath.contains(cdmVolumePath)) {
                    xpath = xpath.replace(cdmVolumePath, cdmVolumePath + "/d:contribution/d:data");
                }
                Log.d(TAG, "XPATH: "  + xpath);
                String[] params = {entry.getContribId(), update, xpath};
                new UpdateContribution().execute(params);
            }
        });

        return v;
    }

    private String getXPath(String tag) {
        SearchActivity sa = (SearchActivity) getActivity();
        return sa.getVolume().getElements().get(tag);
    }

    private class UpdateContribution extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return HTTPUtils.updateContribField(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d(TAG, "OUIIIII");
            } else {
                Log.d(TAG, "NONNNNN");
            }
        }
    }

}
