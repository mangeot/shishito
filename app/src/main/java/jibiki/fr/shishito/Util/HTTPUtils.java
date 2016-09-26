package jibiki.fr.shishito.Util;

import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jibiki.fr.shishito.SearchActivity;

/**
 * Created by tibo on 01/04/15.
 * An utility class to perform HTTP operations
 */
public final class HTTPUtils {

    @SuppressWarnings("unused")
    final static private String TAG = HTTPUtils.class.getSimpleName();

    private HTTPUtils() {
    }

    public static InputStream doGet(String urlStr) throws IOException {
        HttpURLConnection urlConnection;
        InputStream stream;
        URL url = new URL(urlStr);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        stream = urlConnection.getInputStream();

        return stream;

    }

    public static InputStream doPut(String sUrl, String data) throws IOException {
        HttpURLConnection urlConnection;
        InputStream is;
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
        if (urlConnection.getResponseCode() != 201) {
            return null;
        }
        return is;
    }

    public static int doLogin(final String username, final String password) throws IOException {
        HttpURLConnection urlConnection;
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
        return urlConnection.getResponseCode();
    }

    public static String checkLoggedIn() throws ParserConfigurationException, SAXException, IOException {
        HttpURLConnection urlConnection;
        InputStream stream;
        String username;
        URL url = new URL(SearchActivity.SERVER_URL + "UserProfile.po");
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        stream = urlConnection.getInputStream();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(stream);
        Element el = doc.getElementById("CPLogin");
        username = el.getTextContent();

        return username;
    }

}
