package jibiki.fr.shishito;

import android.app.SearchManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Util.XMLUtils;

import static jibiki.fr.shishito.Util.HTTPUtils.doGet;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnWordSelectedListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    // the fragment initialization parameters
    private static final String QUERY = "query";
    private static final String VOLUME = "volume";

    private String query;
    ListView listView;
    ArrayList<ListEntry> curList;
    SearchView searchView;
    TextView noResult;
    private OnWordSelectedListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param query Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String query) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString(QUERY);
        }
        setRetainInstance(true);
    }

    public void updateEntry(ListEntry entry) {
        for (int i = 0; i < curList.size(); i++) {
            ListEntry le = curList.get(i);
            if (le.getEntryId().equals(entry.getEntryId())) {
                EntryListAdapter ela = (EntryListAdapter) listView.getAdapter();
                ela.remove(le);
                ela.insert(entry, i);
                return;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_search, container, false);
        listView = (ListView) v.findViewById(R.id.listView);
        noResult = (TextView) v.findViewById(R.id.noResult);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                mListener.onWordSelected((ListEntry) listView.getItemAtPosition(position));
            }
        });

        if (curList != null) {
            EntryListAdapter adapter = (EntryListAdapter) listView.getAdapter();
            if (adapter == null) {
                adapter = new EntryListAdapter(getActivity(), curList);
                listView.setAdapter(adapter);
            } else {
                adapter.clear();
                adapter.addAll(curList);
                adapter.notifyDataSetChanged();
            }
        }

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) v.findViewById(R.id.action_search);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    EntryListAdapter adapter = (EntryListAdapter) listView.getAdapter();
                    if (adapter != null) {
                        adapter.clear();
                    }
                    curList = null;
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
        });
        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWordSelectedListener) {
            mListener = (OnWordSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        if (query != null) {
            search(query);
        }
    }

    public void search(String query) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = new ShishitoProgressDialog();
            newFragment.show(ft, "dialog");
            new SearchTask().execute(query);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No Network",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnWordSelectedListener {
        // TODO: Update argument type and name
        void onWordSelected(ListEntry entry);
    }

    private class SearchTask extends AsyncTask<String, Void, ArrayList<ListEntry>> {


        public SearchTask() {

        }

        @Override
        protected ArrayList<ListEntry> doInBackground(String... params) {
            InputStream stream = null;
            ArrayList<ListEntry> result = null;
            try {
                String word = URLEncoder.encode(params[0], "UTF-8");
                //stream = doGet(SEREVR_API_URL + "Cesselin/jpn/cdm-headword|cdm-reading|cdm-writing/" + word + "/entries/?strategy=CASE_INSENSITIVE_STARTS_WITH");
                stream = doGet(BaseActivity.SERVER_API_URL + "Cesselin/jpn/cdm-headword|cdm-reading|cdm-writing/" + word + "/entries/?strategy=CASE_INSENSITIVE_EQUAL");
                result = XMLUtils.parseEntryList(stream, ((SearchActivity) getActivity()).getVolume());
                Log.v(TAG, "index=" + result);

            } catch (XmlPullParserException | ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
                e.printStackTrace();
            }
            curList = result;
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<ListEntry> result) {

            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null && prev instanceof ShishitoProgressDialog) {
                ShishitoProgressDialog pd = (ShishitoProgressDialog) prev;
                pd.dismiss();
            }
            if (result == null) {
                Toast.makeText(getActivity().getApplicationContext(), "There was an error!",
                        Toast.LENGTH_SHORT).show();
            } else {
                ListView listView = (ListView) getActivity().findViewById(R.id.listView);
                if (result.size() == 0) {
                    noResult.setVisibility(View.VISIBLE);
                } else {
                    noResult.setVisibility(View.GONE);

                    EntryListAdapter adapter = (EntryListAdapter) listView.getAdapter();
                    if (adapter == null) {
                        adapter = new EntryListAdapter(getActivity(), result);
                        listView.setAdapter(adapter);
                    } else {
                        adapter.clear();
                        adapter.addAll(result);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
