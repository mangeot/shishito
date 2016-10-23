/* Copyright (C) 2016 Thibaut Le Guilly et Mathieu Mangeot
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package jibiki.fr.shishito;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.ViewUtil;

/**
 * A {@link Fragment} subclass that enables display of a word.
 * Use the {@link DisplayEntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayEntryFragment extends Fragment {


    @SuppressWarnings("unused")
    private static final String TAG = DisplayEntryFragment.class.getSimpleName();
    private static final String ENTRY = "entry";
    private ListEntry entry;

//    private OnEditClickListener mListener;

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
//        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    private void updateText(){
        ViewUtil.addVedette(vedette, entry, getActivity(), true);
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
//        if (context instanceof OnEditClickListener) {
//            mListener = (OnEditClickListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        Context ctx = getContext();
//        if (ctx instanceof IsLoggedIn) {
//            IsLoggedIn ili = (IsLoggedIn) ctx;
//            if (ili.isLoggedIn()) {
//                MenuItem item = menu.findItem(R.id.edit_menu);
//                item.setVisible(true);
//            }
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//        if(item.getItemId() == R.id.edit_menu){
//            mListener.onEditClick(entry);
//        }
//        return true;
//    }
//
//    public interface OnEditClickListener {
//        void onEditClick(ListEntry entry);
//    }

}
