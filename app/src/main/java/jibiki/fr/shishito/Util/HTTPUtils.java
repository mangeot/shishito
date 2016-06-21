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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public static boolean updateContribField(String contribId, String update, String xpath) {
        HttpURLConnection urlConnection;
        try {
            URL url = new URL(SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + contribId + "/" + update);
            Log.d(TAG, url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(xpath);
            writer.flush();
            writer.close();
            os.close();
            urlConnection.connect();
            Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
            return false;
        }
        return true;
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

    public static boolean doLoginTest(final String username, final String password, Context context) {
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
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
        } catch (IOException e) {
            Log.d(TAG, "Error:", e);
            if (urlConnection != null) {
                try {
                    Log.d(TAG, "Code:  " + Integer.toString(urlConnection.getResponseCode()));
                } catch (IOException e1) {
                    Log.d(TAG, "Error:", e1);
                }
            }
            return false;
        }

        return true;
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
