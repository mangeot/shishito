package jibiki.fr.shishito.Util;

import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;

/**
 * Created by tibo on 01/04/15.
 * An utility class to perform HTTP operations
 */
public final class HTTPUtils {

    @SuppressWarnings("unused")
    final static private String TAG = HTTPUtils.class.getSimpleName();

    private final static String SERVER_URL = "https://jibiki.imag.fr/jibiki/";
    private final static String SERVER_API_URL = SERVER_URL + "api/";
    private final static String DICT_NAME = "Cesselin";
    private final static String SRC_LANG = "jpn";
    private final static String VOLUME_API_URL = SERVER_API_URL + DICT_NAME + "/" + SRC_LANG + "/";


    private HTTPUtils() {
    }

    public static Volume getRemoteVolume() {
        InputStream stream;
        Volume volume = null;
        try {
            stream = doGet(VOLUME_API_URL);
            volume = XMLUtils.createVolume(stream);
            stream.close();
        } catch (XmlPullParserException | IOException e) {
            return null;
        }
        return volume;
    }

    private static String normalizeQueryString(String string) {
        string = string.replace(" ", "");
        return string;
    }

    private static final Pattern hiraganaPattern = Pattern.compile("[\\p{InHiragana}]+");


    public static ArrayList<ListEntry> getEntryList(String word, Volume volume) {
        InputStream stream;
        ArrayList<ListEntry> result;
        try {
            word = normalizeQueryString(word);
            int firstCharCode = Character.codePointAt(word,1);
            // Si le mot est en romaji (attention, il peut y avoir des macrons (ā = 257 à ū = 360)
            if (firstCharCode < 0x3042) {
                word = ViewUtil.replace_macron(word);
                word = ViewUtil.to_hepburn(word);
                word = URLEncoder.encode(word, "UTF-8");
                stream = doGet(VOLUME_API_URL + "cdm-writing/" + word + "/entries/?strategy=CASE_INSENSITIVE_EQUAL");
            }
            // Si le mot est en hiragana
            else {
                Matcher hiraganaMatcher = hiraganaPattern.matcher(word);
                word = URLEncoder.encode(word, "UTF-8");
                if (hiraganaMatcher.matches()) {
                    stream = doGet(VOLUME_API_URL + "cdm-reading/" + word + "/entries/?strategy=EQUAL");
                }
                // Sinon, mot en japonais (katakana, kanji, ...)
                else {
                    stream = doGet(VOLUME_API_URL + "cdm-headword/" + word + "/entries/?strategy=EQUAL");
                }
            }

            result = XMLUtils.parseEntryList(stream, volume);
            stream.close();

        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            return null;
        }
        return result;
    }

    private static InputStream doGet(String urlStr) throws IOException {
        HttpURLConnection urlConnection;
        InputStream stream;
        URL url = new URL(urlStr);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        stream = urlConnection.getInputStream();

        return stream;

    }

    public static ListEntry updateContribution(String contribId, String text, String xpath, Volume volume) {
        InputStream is;
        ListEntry entry;
        try {
            contribId = java.net.URLEncoder.encode(contribId,"UTF-8");
            text = java.net.URLEncoder.encode(text,"UTF-8");
            String url = VOLUME_API_URL + contribId + "/" + text;
            is = HTTPUtils.doPut(url, xpath);
            entry = XMLUtils.handleListEntryStream(is, volume);
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            return null;
        }

        return entry;
    }


    private static InputStream doPut(String sUrl, String data) throws IOException {
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

    public static int doLogin(final String username, final String password) {
        HttpURLConnection urlConnection = null;
        int res = 0;
        try {
            URL url = new URL(SERVER_URL + "LoginUser.po");
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
            res = urlConnection.getResponseCode();
            urlConnection.getInputStream().close();
            urlConnection.getOutputStream().close();
            urlConnection.disconnect();
        } catch (IOException e) {
            try {
                if (urlConnection != null) {
                    res = urlConnection.getResponseCode();
                } else {
                    return -1;
                }
            } catch (IOException e2) {
                return -1;
            }
        }
        return res;
    }

    public static String checkLoggedIn() throws ParserConfigurationException, SAXException, IOException {
        HttpURLConnection urlConnection = null;
        InputStream stream;
        String username = null;
        try {
            URL url = new URL(SERVER_URL + "UserProfile.po");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            stream = urlConnection.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(stream);
            Element el = doc.getElementById("CPLogin");
            username = el.getTextContent();
            stream.close();
            urlConnection.disconnect();
        } catch (IOException e) {
            if (urlConnection != null && urlConnection.getResponseCode() == 400) {
                return null;
            }
        }

        return username;
    }

}
