package jibiki.fr.shishito;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import jibiki.fr.shishito.Interfaces.OnEntryUpdatedListener;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.Tasks.UpdateContribution;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnEntryUpdatedListener} interface
 * to handle interaction events.
 * Use the {@link FastEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FastEditFragment extends Fragment implements UpdateContribution.ContributionUpdatedListener {

    private static final String TAG = FastEditFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FIELD_CONTENT = "field_content";
    private static final String XPATH = "xpath";
    private static final String CONTRIB_ID = "contrib_id";
    private static final String TITLE = "title";
    private static final String VOLUME = "volume";



    // TODO: Rename and change types of parameters
    private String fieldContent;
    private String xpath;
    private String contribId;
    private String title;

    private Volume volume;


    private Button saveButton;
    private EditText et;

    private OnEntryUpdatedListener mListener;

    public FastEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fieldContent Parameter 1.
     * @param xpath Parameter 2.
     * @return A new instance of fragment FastEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FastEditFragment newInstance(String fieldContent, String xpath, String contribId, String title, Volume volume) {
        FastEditFragment fragment = new FastEditFragment();
        Bundle args = new Bundle();
        args.putString(FIELD_CONTENT, fieldContent);
        args.putString(XPATH, xpath);
        args.putString(CONTRIB_ID, contribId);
        args.putString(TITLE, title);
        args.putSerializable(VOLUME, volume);
        fragment.setArguments(args);
        Log.d(TAG, "CONTRIB: " + contribId);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fieldContent = getArguments().getString(FIELD_CONTENT);
            xpath = getArguments().getString(XPATH);
            contribId = getArguments().getString(CONTRIB_ID);
            title = getArguments().getString(TITLE);
            volume = (Volume)getArguments().getSerializable(VOLUME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fast_edit, container, false);
        TextView tv = (TextView)v.findViewById(R.id.fast_edit_title);
        tv.setText(getString(R.string.fast_edit_title, title));
        et = (EditText)v.findViewById(R.id.fast_edit);
        et.setText(fieldContent);
        saveButton = (Button) v.findViewById(R.id.button_fast);
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
        et.addTextChangedListener(tw);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(false);
                if (!et.getText().toString().equals(fieldContent)) {
                    String[] params = {contribId, et.getText().toString(), xpath};
                    Log.d(TAG, et.getText().toString());
                    new UpdateContribution(FastEditFragment.this, volume).execute(params);
                }
            }
        });
        return v;
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
    public void onContributionUpdated(ListEntry entry) {
        if (entry != null) {
            mListener.onEntryUpdatedListener(entry);
        }
    }
}
