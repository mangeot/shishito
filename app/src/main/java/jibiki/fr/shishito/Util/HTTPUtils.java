package jibiki.fr.shishito.Util;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jibiki.fr.shishito.SearchActivity;

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

    public static InputStream doPut(String sUrl, String data) {
        HttpURLConnection urlConnection = null;
        InputStream is;
        Log.d(TAG, data);
         try {
            URL url = new URL(sUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            os.close();
            urlConnection.connect();
            is = urlConnection.getInputStream();
            Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
            if (urlConnection.getResponseCode() != 201) {
                return null;
            }
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
            try {
                Log.d(TAG, "ERROR: " + urlConnection.getResponseCode());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        return is;
    }

    public static int doLoginTest(final String username, final String password, Context context) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(SearchActivity.SERVER_URL + "LoginUser.po");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("Login", username)
                    .appendQueryParameter("Password", password)
                    .appendQueryParameter("RememberLogin", "on")
                    .appendQueryParameter("NoRedirection", "on")
                    .appendQueryParameter("Submit", "Login");
            String query = builder.build().getEncodedQuery();

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            urlConnection.connect();
            Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
            return urlConnection.getResponseCode();
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
            return -1;
        }
    }

    public static String checkLoggedIn() {
        HttpURLConnection urlConnection;
        InputStream stream;
        String username;
        try {
            URL url = new URL(SearchActivity.SERVER_URL + "UserProfile.po");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            stream = urlConnection.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(stream);
            Element el = doc.getElementById("CPLogin");
            username = el.getTextContent();

        } catch (IOException | ParserConfigurationException | SAXException e) {
            Log.d(TAG, "Error:", e);
            return null;
        }

        return username;
    }

}
