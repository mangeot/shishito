package jibiki.fr.shishito;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jibiki.fr.shishito.Util.XMLUtils;

import static jibiki.fr.shishito.Util.HTTPUtils.doGet;


public class SearchActivity extends ActionBarActivity {

    private static final String TAG = "SearchActivity";

    ListView listView;

    private Dictionary dictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = (ListView) findViewById(R.id.listView);

        SearchView searchView = (SearchView) findViewById(R.id.action_search);
        //searchView.setSubmitButtonEnabled(true);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new InitDictionaryTask().execute();
        } else {
            Toast.makeText(getApplicationContext(), "No Network",
                    Toast.LENGTH_SHORT).show();
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    public void search(String query) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SearchTask().execute(query);
        } else {
            Toast.makeText(getApplicationContext(), "No Network",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                stream = doGet("http://jibiki.fr/jibiki/api/Cesselin/jpn/cdm-headword|cdm-reading|cdm-writing/" + word + "/entries");
                result = XMLUtils.parseEntryList(stream, dictionary);
                Log.v(TAG, "index=" + result);

            } catch (XmlPullParserException | ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<ListEntry> result) {

            if (result == null) {
                Toast.makeText(getApplicationContext(), "There was an error!",
                        Toast.LENGTH_SHORT).show();
            } else {

                EntryListAdapter adapter = (EntryListAdapter) listView.getAdapter();
                if(adapter == null){
                    adapter = new EntryListAdapter(SearchActivity.this, result.toArray(
                            new ListEntry[result.size()]));
                    listView.setAdapter(adapter);
                }else {
                    adapter.clear();
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class InitDictionaryTask extends AsyncTask<String, Void, Dictionary> {


        public InitDictionaryTask() {

        }

        @Override
        protected Dictionary doInBackground(String... params) {
            InputStream stream = null;
            Dictionary dict = null;
            try {
                stream = doGet("http://jibiki.fr/jibiki/api/Cesselin/jpn/");
                dict = XMLUtils.createDictionary(stream);

                Log.v(TAG, "index=" + dict);

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return dict;
        }

        @Override
        protected void onPostExecute(Dictionary dict) {
            SearchActivity.this.dictionary = dict;
        }
    }
}
