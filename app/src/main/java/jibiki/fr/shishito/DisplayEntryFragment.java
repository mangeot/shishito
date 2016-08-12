package jibiki.fr.shishito;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.Util.ViewUtil;

public class DisplayEntryFragment extends Fragment {


    private static final String TAG = DisplayEntryFragment.class.getSimpleName();
    private static final String ENTRY = "entry";
    private static final String VOLUME = "volume";

    private static final int EDIT_MENU_ID = 10;

    private ListEntry entry;

    private OnEditClickListener mListener;

    private boolean loggedIn = false;

    TextView vedette;
    View v;


    public DisplayEntryFragment() {
    }

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
    }

    private void updateText(){
        ViewUtil.addVedette(vedette, entry, loggedIn, getContext());
        ViewUtil.addVerified(v, entry);
        ViewUtil.parseAndAddGramBlocksToView(v, entry, getContext(), loggedIn);
        ViewUtil.parseAndAddExamplesToView(v, entry, getContext(), loggedIn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState) {

        View relative = inflater.inflate(R.layout.activity_display_entry, container, false);
        v = relative.findViewById(R.id.linearlayout);
        vedette = (TextView) v.findViewById(R.id.vedette);
        if (!TextUtils.isEmpty(((SearchActivity)getActivity()).getUsername())) {
            loggedIn = true;
        }
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
        MenuItem login = menu.findItem(R.id.action_sign_in);
        if(!login.getTitle().equals(getString(R.string.action_sign_in_short)) && menu.findItem(EDIT_MENU_ID) == null) {
            MenuItem item = menu.add(Menu.NONE, EDIT_MENU_ID, Menu.NONE, R.string.edit_menu_item);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == EDIT_MENU_ID){
            mListener.onEditClick(entry);
        }
        return true;
    }

    public interface OnEditClickListener {
        void onEditClick(ListEntry entry);
    }

}
