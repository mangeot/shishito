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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.xml.parsers.ParserConfigurationException;

import jibiki.fr.shishito.Interfaces.FastEditListener;
import jibiki.fr.shishito.Interfaces.OnEntryUpdatedListener;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.Util.PersistentCookieStore;
import jibiki.fr.shishito.Util.XMLUtils;

import static jibiki.fr.shishito.Util.HTTPUtils.checkLoggedIn;
import static jibiki.fr.shishito.Util.HTTPUtils.doGet;


public class SearchActivity extends AppCompatActivity implements SearchFragment.OnWordSelectedListener,
        OnEntryUpdatedListener, FastEditListener {

    @SuppressWarnings("unused")
    private static final String TAG = SearchActivity.class.getSimpleName();
    //public final static String SERVER_URL = "http://jibiki.fr/jibiki/";
    // Il y a une version sécurisée. On l'active par défaut ? => oui
    public final static String SERVER_URL = "https://jibiki.imag.fr/jibiki/";
    public final static String SERVER_API_URL = SERVER_URL + "api/";
    public final static String DICT_NAME = "Cesselin";
    public final static String SRC_LANG = "jpn";
    public final static String VOLUME_API_URL = SERVER_API_URL + DICT_NAME + "/" + SRC_LANG + "/";


    public final static String USERNAME = "jibiki.fr.shishito.USERNAME";
    public final static String VOLUME = "jibiki.fr.shishito.VOLUME";

    public final static int USERNAME_RESULT = 1;

    private Menu menu;
    private String username = "";

    private Volume volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (savedInstanceState == null) {

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
                Toast.makeText(getApplicationContext(), R.string.no_network,
                        Toast.LENGTH_SHORT).show();
            }

            putSearchFragment(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(USERNAME, username);
        savedInstanceState.putSerializable(VOLUME, volume);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        username = savedInstanceState.getString(USERNAME);
        volume = (Volume) savedInstanceState.getSerializable(VOLUME);

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
        switch (item.getItemId()) {
            case R.id.action_sign_in:
                if (TextUtils.isEmpty(username)) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    this.startActivityForResult(intent, USERNAME_RESULT);
                }
                return true;
            case R.id.action_sign_out:
                logOut();
                return true;
            case R.id.action_about:
                putAboutFragment();
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
                setLoggedIn();
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

            if (frag != null && frag instanceof SearchFragment) {
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
        SearchFragment sf = SearchFragment.newInstance(query, volume);
        makeTransaction(sf, "search", false);
    }

    private void putAboutFragment() {
        AboutFragment def = AboutFragment.newInstance();
        makeTransaction(def, "about", true);
    }

    private void makeTransaction(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, tag);
        if (addToBackStack) transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void putDisplayEntryFragment(ListEntry entry) {
        DisplayEntryFragment def = DisplayEntryFragment.newInstance(entry);
        makeTransaction(def, "display", true);
    }

    private void putEditFragment(ListEntry entry) {
        EditFragment ef = EditFragment.newInstance(entry);
        makeTransaction(ef, "edit", true);
    }

    @Override
    public void putFastEdit(String contribId, String xPath, String content, String title) {
        FastEditFragment fef = FastEditFragment.newInstance(content, xPath, contribId, title, volume);
        makeTransaction(fef, "fast", true);
    }

    private void setLoggedIn() {
        if (!TextUtils.isEmpty(username)) {
            MenuItem item = menu.findItem(R.id.action_sign_in);
            item.setTitle(username);
            item = menu.findItem(R.id.action_sign_out);
            item.setVisible(true);
//            Fragment f = getSupportFragmentManager().findFragmentByTag("display");
//            if (f != null && f.isVisible()) {
//                item = menu.findItem(R.id.edit_menu);
//                item.setVisible(true);
//            }
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
//        item = menu.findItem(R.id.edit_menu);
//        item.setVisible(false);
    }

    @Override
    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(username);
    }

    public Volume getVolume() {
        return this.volume;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public void onWordSelected(ListEntry entry) {
        putDisplayEntryFragment(entry);
    }

//    @Override
//    public void onEditClick(ListEntry entry) {
//        putEditFragment(entry);
//    }

    @Override
    public void onEntryUpdatedListener(ListEntry entry) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag("display");
        if (frag != null && frag instanceof DisplayEntryFragment) {
            DisplayEntryFragment def = (DisplayEntryFragment) frag;
            def.setListEntry(entry);
        }

        frag = getSupportFragmentManager().findFragmentByTag("search");
        if (frag != null && frag instanceof SearchFragment) {
            SearchFragment sf = (SearchFragment) frag;
            sf.updateEntry(entry);
        }
        getSupportFragmentManager().popBackStack();
    }

    private class InitVolumeTask extends AsyncTask<String, Void, Volume> {


        InitVolumeTask() {

        }

        @Override
        protected Volume doInBackground(String... params) {
            InputStream stream;
            Volume volume = null;
            try {
                stream = doGet(VOLUME_API_URL);
                volume = XMLUtils.createVolume(stream);
            } catch (XmlPullParserException | IOException e) {
                Toast.makeText(getApplicationContext(), R.string.error,
                        Toast.LENGTH_SHORT).show();
            }
            return volume;
        }

        @Override
        protected void onPostExecute(Volume volume) {
            SearchActivity.this.volume = volume;
        }
    }

    private class CheckLoggedIn extends AsyncTask<Void, Void, String> {


        CheckLoggedIn() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String username = "";

            try {
                username = checkLoggedIn();
            } catch (IOException | ParserConfigurationException | SAXException e) {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return username;
        }

        @Override
        protected void onPostExecute(String userName) {
            username = userName;
            setLoggedIn();
        }
    }
}
