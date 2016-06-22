package jibiki.fr.shishito;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.HTTPUtils;
import jibiki.fr.shishito.Util.XMLUtils;

public class EditFragment extends Fragment {

    private static final String TAG = EditFragment.class.getSimpleName();

    private static final String ENTRY = "entry";

    private ListEntry entry;
    private Button saveButton;

    private OnEntryUpdatedListener mListener;

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

    public interface OnEntryUpdatedListener {
        // TODO: Update argument type and name
        void onEntryUpdatedListener(ListEntry entry);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEntryUpdatedListener) {
            mListener = (OnEntryUpdatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                String cdmElement;
                String update;
                ArrayList<Pair<String, String>> xpaths = new ArrayList<>(4);
                saveButton.setEnabled(false);
                checkAddPairToArrayList(xpaths, entry.getKanji(), "cdm-headword", R.id.kanji, v);
                checkAddPairToArrayList(xpaths, entry.getHiragana(), "cdm-reading", R.id.hiragana, v);
                checkAddPairToArrayList(xpaths, entry.getRomanji(), "cdm-writing", R.id.romaji, v);
                checkAddPairToArrayList(xpaths, entry.getDefinition(), "cdm-definition", R.id.definition, v);

                Log.d(TAG, "XPATHS SIZE: " + xpaths.size());
                if (xpaths.size() == 0) {
                    makeToast("Pas de changement détecté.");
                } else if (xpaths.size() == 1) {
                    Pair<String, String> p = xpaths.get(0);
                    cdmElement = p.first;
                    update = p.second;
                    String xpath = ((SearchActivity) getActivity()).getVolume().getElements().get(cdmElement);
                    String cdmVolumePath = ((SearchActivity) getActivity()).getVolume().getElements().get("cdm-volume");
                    if (cdmElement.contains(cdmVolumePath)) {
                        xpath = xpath.replace(cdmVolumePath, cdmVolumePath + "/d:contribution/d:data");
                    }
                    String[] params = {entry.getContribId(), update, xpath};
                    new UpdateContribution().execute(params);
                } else {
                    new PrepareUpdateEntry().execute(xpaths);
                }

            }
        });

        return v;
    }

    private void checkAddPairToArrayList(ArrayList<Pair<String, String>> a, String value, String tag, int id, View v) {
        if (!value.equals(((EditText) v.findViewById(id)).getText().toString())) {
            String cdmElement = tag;
            String update = ((EditText) v.findViewById(id)).getText().toString();
            a.add(new Pair<>(cdmElement, update));
        }
    }

    private String getXPath(String tag) {
        SearchActivity sa = (SearchActivity) getActivity();
        return sa.getVolume().getElements().get(tag);
    }

    private void onEntryModified(ListEntry entry) {
        this.mListener.onEntryUpdatedListener(entry);
    }

    private ListEntry handleListEntryStream(InputStream stream) {
        ListEntry entry = null;
        if (stream != null) {
            try {
                entry = XMLUtils.parseEntryStream(stream, ((SearchActivity) getActivity()).getVolume());
                Log.d(TAG, "ENTRY IS: " + entry.getDefinition());
            } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
                Log.e(TAG, "Error parsing entry stream: " + e.getMessage());
            }
        } else {
            return null;
        }

        return entry;
    }

    private void handleListEntry(ListEntry entry) {
        if (entry != null) {
            EditFragment.this.onEntryModified(entry);
        } else {
            saveError();
            EditFragment.this.saveButton.setEnabled(true);
        }
    }

    private void saveError() {
        makeToast("Erreur, les modifications n'ont pas été sauvegardés.");
    }

    private void makeToast(String msg) {
        Toast t = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        t.setGravity(Gravity.TOP, 0, 10);
        t.show();
    }

    private class UpdateContribution extends AsyncTask<String, Void, ListEntry> {

        @Override
        protected ListEntry doInBackground(String... params) {
            String url = SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + params[0] + "/" + params[1];
            InputStream is = HTTPUtils.doPut(url, params[2]);
            return handleListEntryStream(is);
        }

        @Override
        protected void onPostExecute(ListEntry entry) {
            handleListEntry(entry);
        }
    }

    private class PrepareUpdateEntry extends AsyncTask<ArrayList<Pair<String, String>>, Void, String> {
        @Override
        protected String doInBackground(ArrayList<Pair<String, String>>... params) {
            String url = SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + EditFragment.this.entry.getContribId() + "/";
            InputStream stream = HTTPUtils.doGet(url);
            String res;
            try {
                res = XMLUtils.updateEntryXmlFromStream(stream, params[0], ((SearchActivity) getActivity()).getVolume());
            } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
                Log.e(TAG, "Error updating entry from stream: " + e.getMessage());
                return null;
            }

            return res;
        }

        @Override
        protected void onPostExecute(String res) {
            if (res != null) {
                new UpdateEntry().execute(res);
            } else {
                saveError();
            }
        }
    }

    private class UpdateEntry extends AsyncTask<String, Void, ListEntry> {

        @Override
        protected ListEntry doInBackground(String... params) {
            String url = SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + EditFragment.this.entry.getEntryId() + "/";
            InputStream stream = HTTPUtils.doPut(url, params[0]);
            return handleListEntryStream(stream);
        }

        @Override
        protected void onPostExecute(ListEntry entry) {
            handleListEntry(entry);
        }
    }

}
