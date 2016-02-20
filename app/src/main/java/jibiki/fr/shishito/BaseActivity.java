package jibiki.fr.shishito;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.InputStream;

import jibiki.fr.shishito.Models.Volume;

import static jibiki.fr.shishito.Util.HTTPUtils.doLoginTest;

/**
 * Created by tibo on 08/01/16.
 */
public class BaseActivity extends ActionBarActivity {

    public final static String USERNAME = "jibiki.fr.shishito.USERNAME";
    public final static String PASSWORD = "jibiki.fr.shishito.PASSWORD";
    public final static String SERVER_URL = "http://jibiki.fr/jibiki/";
    // pour tester avec Jibiki installé en local
    //public final static String SERVER_URL = "http://10.0.2.2:8999/jibiki/";
    public final static String SERVER_API_URL = SERVER_URL + "api/";

    public final static int USERNAME_RESULT = 1;

    ActionBar actionBar;
    Menu menu;

    String username = "";
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop(){
        Intent intent = new Intent();
        intent.putExtra(SearchActivity.USERNAME, username);
        intent.putExtra(SearchActivity.PASSWORD, password);
        setResult(RESULT_OK, intent);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_bar, menu);

        Intent intent = getIntent();
        username = intent.getStringExtra(USERNAME);
        if (username != null && !username.isEmpty()) {
            MenuItem item = menu.findItem(R.id.action_sign_in);
            item.setTitle(username);
        }
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
        if (id == R.id.action_sign_in) {
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivityForResult(intent, USERNAME_RESULT);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USERNAME_RESULT) {
            if (resultCode == RESULT_OK && data != null) {
                username = data.getStringExtra(USERNAME);
                password = data.getStringExtra(PASSWORD);
                if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                    MenuItem item = menu.findItem(R.id.action_sign_in);
                    item.setTitle(username);
//                    new TestPutTask().execute();
                }
            }
        }
    }



}

