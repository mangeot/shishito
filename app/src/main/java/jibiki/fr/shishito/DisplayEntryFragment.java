package jibiki.fr.shishito;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jibiki.fr.shishito.Interfaces.IsLoggedIn;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.ViewUtil;

/**
 * A {@link Fragment} subclass that enables display of a word.
 * Activities that contain this fragment must implement the
 * {@link OnEditClickListener} interface
 * to handle interaction events.
 * Use the {@link DisplayEntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayEntryFragment extends Fragment {


    @SuppressWarnings("unused")
    private static final String TAG = DisplayEntryFragment.class.getSimpleName();
    private static final String ENTRY = "entry";
    private ListEntry entry;

    private OnEditClickListener mListener;

    private TextView vedette;
    private View v;


    public DisplayEntryFragment() {
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param entry The entry to be displayed.
     * @return A new instance of fragment SearchFragment.
     */
    public static DisplayEntryFragment newInstance(ListEntry entry) {
        DisplayEntryFragment fragment = new DisplayEntryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ENTRY, entry);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListEntry(ListEntry listEntry) {
        this.entry = listEntry;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entry = (ListEntry) getArguments().getSerializable(ENTRY);
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    private void updateText(){
        ViewUtil.addVedette(vedette, entry, getContext(), true);
        ViewUtil.addVerified(v, entry);
        ViewUtil.parseAndAddGramBlocksToView(v, entry, getContext(), true);
        ViewUtil.parseAndAddExamplesToView(v, entry, getContext(), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState) {

        View relative = inflater.inflate(R.layout.activity_display_entry, container, false);
        v = relative.findViewById(R.id.linearlayout);
        vedette = (TextView) v.findViewById(R.id.vedette);
        updateText();
        return relative;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEditClickListener) {
            mListener = (OnEditClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Context ctx = getContext();
        if (ctx instanceof IsLoggedIn) {
            IsLoggedIn ili = (IsLoggedIn) ctx;
            if (ili.isLoggedIn()) {
                MenuItem item = menu.findItem(R.id.edit_menu);
                item.setVisible(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.edit_menu){
            mListener.onEditClick(entry);
        }
        return true;
    }

    public interface OnEditClickListener {
        void onEditClick(ListEntry entry);
    }

}
