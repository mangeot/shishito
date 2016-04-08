package jibiki.fr.shishito;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import jibiki.fr.shishito.Models.ListEntry;

public class EditFragment extends Fragment {

    private static final String ENTRY = "entry";

    private ListEntry entry;

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
        View v = inflater.inflate(R.layout.activity_edit, container, false);

        EditText editText = (EditText)v.findViewById(R.id.kanji);
        editText.setText(entry.getKanji());
        editText = (EditText)v.findViewById(R.id.hiragana);
        editText.setText(entry.getHiragana());
        editText = (EditText)v.findViewById(R.id.romaji);
        editText.setText(entry.getRomanji());
        editText = (EditText)v.findViewById(R.id.gram);
        editText.setText(entry.getGram());

        return v;
    }
}
