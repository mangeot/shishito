package jibiki.fr.shishito;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.List;

import jibiki.fr.shishito.Models.Dictionary;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.Util.PersistentCookieStore;
import jibiki.fr.shishito.Util.XMLUtils;

import static jibiki.fr.shishito.Util.HTTPUtils.checkLoggedIn;
import static jibiki.fr.shishito.Util.HTTPUtils.doGet;


public class SearchActivity extends AppCompatActivity implements SearchFragment.OnWordSelectedListener, DisplayEntryFragment.OnEditClickListener {

    private static final String TAG = "SearchActivity";
    public final static String ENTRY = "jibiki.fr.shishito.ENTRY";
    public final static String SERVER_URL = "http://jibiki.fr/jibiki/";
    public final static String SERVER_API_URL = SERVER_URL + "api/";

    public final static String USERNAME = "jibiki.fr.shishito.USERNAME";

    public final static int USERNAME_RESULT = 1;


    ListView listView;
    Menu menu;
    ListEntry curEntry;

    String username = "";

    private Dictionary dictionary;
    private Volume volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //searchView.setSubmitButtonEnabled(true);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            PersistentCookieStore pcs = new PersistentCookieStore(this);
            CookieManager cm = new CookieManager(pcs, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cm);
            new InitVolumeTask().execute();
            new CheckLoggedIn().execute();
        } else {
            Toast.makeText(getApplicationContext(), "No Network",
                    Toast.LENGTH_SHORT).show();
        }


        handleIntent(getIntent());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_bar, menu);

        setLoggedIn();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {

            case R.id.action_settings:
                return true;
            case R.id.action_sign_in:
                if(TextUtils.isEmpty(username)) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    this.startActivityForResult(intent, USERNAME_RESULT);
                }
                return true;
            case R.id.action_sign_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USERNAME_RESULT) {
            if (resultCode == RESULT_OK && data != null) {
                username = data.getStringExtra(USERNAME);
                if (username != null && !username.isEmpty()) {
                    MenuItem item = menu.findItem(R.id.action_sign_in);
                    item.setTitle(username);
//                    new TestPutTask().execute();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (frag != null && frag instanceof  SearchFragment) {
                SearchFragment sf = (SearchFragment) frag;
                sf.search(query);
            } else {
                putSearchFragment(query);
            }
        } else {
            putSearchFragment(null);
        }
    }

    private void putSearchFragment(String query) {
        SearchFragment sf = SearchFragment.newInstance(query);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, sf);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void putDisplayEntryFragment(ListEntry entry) {
        DisplayEntryFragment def = DisplayEntryFragment.newInstance(entry);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, def);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void putEditFragment(ListEntry entry) {
        EditFragment ef = EditFragment.newInstance(entry);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, ef);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void setLoggedIn() {
        if (!TextUtils.isEmpty(username)) {
            MenuItem item = menu.findItem(R.id.action_sign_in);
            item.setTitle(username);
            item = menu.findItem(R.id.action_sign_out);
            item.setVisible(true);
        }
    }

    private void logOut() {
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        cm.getCookieStore().removeAll();
        username = "";
        MenuItem item = menu.findItem(R.id.action_sign_in);
        item.setTitle(R.string.action_sign_in_short);
        item = menu.findItem(R.id.action_sign_out);
        item.setVisible(false);
    }

    public Volume getVolume(){
        return this.volume;
    }

    @Override
    public void onWordSelected(ListEntry entry) {
        putDisplayEntryFragment(entry);
    }

    @Override
    public void onEditClick(ListEntry entry) {
        putEditFragment(entry);
    }

    private class InitVolumeTask extends AsyncTask<String, Void, Volume> {


        public InitVolumeTask() {

        }

        @Override
        protected Volume doInBackground(String... params) {
            InputStream stream = null;
            Volume volume = null;
            try {
                stream = doGet(SERVER_API_URL + "Cesselin/jpn/");
                volume = XMLUtils.createVolume(stream);

                Log.v(TAG, "index=" + volume);

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return volume;
        }

        @Override
        protected void onPostExecute(Volume volume) {
            SearchActivity.this.volume = volume;
        }
    }

    private class CheckLoggedIn extends AsyncTask<Void, Void, String> {


        public CheckLoggedIn() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String username = "";

            username = checkLoggedIn();

            return username;
        }

        @Override
        protected void onPostExecute(String userName) {
            username = userName;
            setLoggedIn();
        }
    }
}
