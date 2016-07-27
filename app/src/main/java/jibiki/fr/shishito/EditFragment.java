package jibiki.fr.shishito;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.GramBlock;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.HTTPUtils;
import jibiki.fr.shishito.Util.XMLUtils;

public class EditFragment extends Fragment {

    private static final String TAG = EditFragment.class.getSimpleName();

    private static final String ENTRY = "entry";

    private static final int KANJI = 100;
    private static final int HIRAGANA = KANJI + 1;
    private static final int ROMAJI_DISPLAY = HIRAGANA + 1;
    private static final int ROMAJI_SEARCH = ROMAJI_DISPLAY + 1;
    
    private ListEntry entry;
    private Button saveButton;

    private OnEntryUpdatedListener mListener;

    private ArrayList<Pair<String, String>> modifWaitList;

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

    private void addFieldVerif(LinearLayout ll, boolean verif, String text, String title, TextWatcher tw, int id) {
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView tv;
        if (verif) {
            tv = new TextView(getContext());
            TextViewCompat.setTextAppearance(tv, android.R.style.TextAppearance_Medium);
            params1.setMargins(15, 0, 0, 0);
            if (TextUtils.isEmpty(text)) {
                text = getString(R.string.empty_field);
                tv.setTypeface(null, Typeface.ITALIC);
            }
        } else {
            tv = new EditText(getContext());
            tv.addTextChangedListener(tw);
        }
        tv.setId(id);
        tv.setText(text);
        tv.setLayoutParams(params1);
        addTitleView(title, ll);
        ll.addView(tv);
    }

    private void addTitleView(String title, LinearLayout ll) {
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setLayoutParams(titleParams);
        TextViewCompat.setTextAppearance(titleView, android.R.style.TextAppearance_Medium);
        ll.addView(titleView);
    }

    private void addEditView(String content, LinearLayout ll, int num, String cdm) {
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText et = new EditText(getContext());
        et.setLayoutParams(editParams);
        et.setText(content);
        ll.addView(et);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_edit, container, false);
        LinearLayout ll = (LinearLayout) v.findViewById(R.id.editlinear);
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

        addFieldVerif(ll, entry.isVerified(), entry.getKanji(), getString(R.string.kanji), tw, KANJI);
        addFieldVerif(ll, entry.isVerified(), entry.getHiragana(), getString(R.string.hiragana), tw, HIRAGANA);
        addFieldVerif(ll, entry.isVerified(), entry.getRomajiDisplay(), getString(R.string.romaji), tw, ROMAJI_DISPLAY);
        addFieldVerif(ll, entry.isVerified(), entry.getRomajiSearch(), getString(R.string.romaji_search), tw, ROMAJI_SEARCH);

        for (GramBlock gram: entry.getGramBlocks()) {
            addTitleView("[" + gram.getGram() + "]", ll);
            int i = 1;
            for (String sense: gram.getSens()) {
                addTitleView("Sens " + i + ":", ll);
                addEditView(sense, ll, i, "cdm-definition");
                i++;
            }
        }

        int i = 1;
        for (Example ex : entry.getExamples()) {
            addTitleView(getString(R.string.editexample, i), ll);
            addEditView(ex.getHiragana(), ll, i, "cesselin-example-hiragana");
            addEditView(ex.getRomaji(), ll, i, "cesselin-example-romaji");
            addEditView(ex.getFrench(), ll, i, "cdm-example");
            i++;
        }

        saveButton = (Button) v.findViewById(R.id.button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View but) {
                ArrayList<Pair<String, String>> xpaths = new ArrayList<>(4);
                saveButton.setEnabled(false);
                if (!entry.isVerified()) {
                    checkAddPairToArrayList(xpaths, entry.getKanji(), "cdm-headword", KANJI, v);
                    checkAddPairToArrayList(xpaths, entry.getHiragana(), "cdm-reading", HIRAGANA, v);
                    checkAddPairToArrayList(xpaths, entry.getRomajiDisplay(), "cdm-writing", ROMAJI_DISPLAY, v);
                    checkAddPairToArrayList(xpaths, entry.getRomajiSearch(), "cdm-writing", ROMAJI_SEARCH, v);
                }
//                checkAddPairToArrayList(xpaths, entry.getDefinition(), "cdm-definition", R.id.definition, v);
//                checkAddPairToArrayList(xpaths, entry.getGram(), "cdm-pos", R.id.gram, v);


                if (xpaths.size() == 0) {
                    makeToast("Pas de changement détecté.");
                } else{ //if (xpaths.size() == 1) {
                    EditFragment.this.modifWaitList = xpaths;
                    EditFragment.this.doNextModif();
                }
//                else {
//                    new PrepareUpdateEntry().execute(xpaths);
//                }

            }
        });

        return v;
    }

    private void doNextModif() {
        Pair<String, String> p = this.modifWaitList.get(0);
        this.modifWaitList.remove(0);
        String cdmElement;
        String update;
        cdmElement = p.first;
        update = p.second;
        String xpath = ((SearchActivity) getActivity()).getVolume().getElements().get(cdmElement);
        String cdmVolumePath = ((SearchActivity) getActivity()).getVolume().getElements().get("cdm-volume");

        if (xpath.contains(cdmVolumePath)) {
            xpath = xpath.replace(cdmVolumePath, cdmVolumePath + "/d:contribution/d:data");
        }

        String[] params = {entry.getContribId(), update, xpath};
        new UpdateContribution().execute(params);
    }

    private void checkAddPairToArrayList(ArrayList<Pair<String, String>> a, String value, String tag, int id, View v) {

        if (!value.equals(((EditText) v.findViewById(id)).getText().toString())) {
            String update = ((EditText) v.findViewById(id)).getText().toString();
            a.add(new Pair<>(tag, update));
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
            if (EditFragment.this.modifWaitList.size() == 0) {
                handleListEntry(entry);
            } else {
                EditFragment.this.entry = entry;
                EditFragment.this.doNextModif();
            }
        }
    }

/**Keep this part in case want to change back to update entire entry**/
//    private class PrepareUpdateEntry extends AsyncTask<ArrayList<Pair<String, String>>, Void, String> {
//        @Override
//        protected String doInBackground(ArrayList<Pair<String, String>>... params) {
//            String url = SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + EditFragment.this.entry.getContribId() + "/";
//            InputStream stream = HTTPUtils.doGet(url);
//            String res;
//            try {
//                res = XMLUtils.updateEntryXmlFromStream(stream, params[0], ((SearchActivity) getActivity()).getVolume());
//            } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
//                Log.e(TAG, "Error updating entry from stream: " + e.getMessage());
//                return null;
//            }
//
//            return res;
//        }
//
//        @Override
//        protected void onPostExecute(String res) {
//            if (res != null) {
//                new UpdateEntry().execute(res);
//            } else {
//                saveError();
//            }
//        }
//    }
//
//    private class UpdateEntry extends AsyncTask<String, Void, ListEntry> {
//
//        @Override
//        protected ListEntry doInBackground(String... params) {
//            String url = SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + EditFragment.this.entry.getEntryId() + "/";
//            InputStream stream = HTTPUtils.doPut(url, params[0]);
//            return handleListEntryStream(stream);
//        }
//
//        @Override
//        protected void onPostExecute(ListEntry entry) {
//            handleListEntry(entry);
//        }
//    }

}
