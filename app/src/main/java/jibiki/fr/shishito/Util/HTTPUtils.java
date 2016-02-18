package jibiki.fr.shishito.Util;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by tibo on 01/04/15.
 */
public final class HTTPUtils {

    final static private String TAG = HTTPUtils.class.getSimpleName();

    private HTTPUtils() {
    }

    public static InputStream doGet(String urlStr) {
        HttpURLConnection urlConnection;
        InputStream stream = null;
        StringBuilder total = new StringBuilder();
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

    public static String doGetString(String urlStr) {
        HttpURLConnection urlConnection;
        InputStream stream = null;
        StringBuilder total = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            stream = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
        }
        return total.toString();

    }

    public static InputStream doLoginTest(final String username, final String password) {
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        try {
            URL url = new URL("http://jibiki.fr/jibiki/LoginUser.po'");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Accept", "*/*");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String data = "Login=" + username + "&Password=" + password + "&RememberLogin=on&Submit=Login";
            String encoded  = URLEncoder.encode(data,"UTF-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(encoded.length()));
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(encoded.getBytes("UTF-8"));
            stream = new DataInputStream(urlConnection.getInputStream());
            Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
            if (urlConnection != null) {
                try {
                    Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
                } catch (IOException e1) {
                    Log.d(TAG, "Error:", e1);
                }
            }
        }
        return stream;
    }

    public static InputStream doLoginTest2(final String username, final String password) {
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        StringBuilder total = new StringBuilder();
        String data = "/volume/d:contribution/d:data/article/forme/vedette";
        try {
            URL url = new URL("http://totoro.imag.fr/lexinnova/api/Lexinnovathibaut/esp/esp.toto.1005389.c/tata");

//            Authenticator.setDefault(new Authenticator(){
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(username,password.toCharArray());
//                }});
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("GET");
            String encoded = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
            Log.d(TAG, encoded);

            urlConnection.setRequestProperty("Authorization", encoded);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.setRequestProperty("Accept", "application/xml;charset=UTF-8");
//            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(data.length()));
            urlConnection.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(data.getBytes("UTF-8"));
            stream = new DataInputStream(urlConnection.getInputStream());
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
            if (urlConnection != null) {
                try {
                    Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
                } catch (IOException e1) {
                    Log.d(TAG, "Error:", e1);
                }
            }
        }
        return stream;
    }

}
