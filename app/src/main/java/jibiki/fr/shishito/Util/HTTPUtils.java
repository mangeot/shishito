package jibiki.fr.shishito.Util;

import android.util.Log;

import org.apache.http.HttpVersion;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tibo on 01/04/15.
 */
public final class HTTPUtils {

    final static private String TAG = HTTPUtils.class.getSimpleName();

    private HTTPUtils() {
    }

    public static InputStream doGet(String urlStr){
        HttpURLConnection urlConnection;
        InputStream stream = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            stream = urlConnection.getInputStream();
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
        }
        return stream;

    }
}
