package jibiki.fr.shishito.Util;

import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jibiki.fr.shishito.Dictionary;
import jibiki.fr.shishito.ListEntry;

/**
 * Created by tibo on 17/09/15.
 */
public final class XMLUtils {

    private static final String ns = null;
    private static final String TAG = XMLUtils.class.getSimpleName();


    private XMLUtils() {
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static String readEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        String entry = "";
        parser.require(XmlPullParser.START_TAG, ns, "entry");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getName().equals("criteria")) {
                if (parser.next() == XmlPullParser.TEXT) {
                    entry = parser.getText();
                }
                //Move to end of criteria tag
                parser.next();
                parser.require(XmlPullParser.END_TAG, ns, "criteria");
            } else {
                skip(parser);
            }
        }
        return entry;
    }

    public static ArrayList<ListEntry> parseEntryList(InputStream stream, Dictionary dict) throws IOException, XmlPullParserException, XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        ArrayList<ListEntry> entries = new ArrayList<ListEntry>();
        InputSource is = new InputSource(stream);
        //is.setEncoding("ISO-8859-1");
        is.setEncoding("UTF-8");
        NodeList shows = (NodeList) xPath.evaluate(
                ((String) dict.getElements().get("cdm-entry"))
                , is, XPathConstants.NODESET);
        for (int i = 0; i < shows.getLength(); i++) {
            Element show = (Element) shows.item(i);
            ListEntry entry = new ListEntry();
            entry.setKanji(xPath.evaluate(
                    ((String) dict.getElements().get("cdm-headword")), show));
            entry.setHiragana(xPath.evaluate(
                    ((String) dict.getElements().get("cdm-writing")), show));
            entry.setRomanji(xPath.evaluate(
                    ((String) dict.getElements().get("cdm-reading")), show));
            entries.add(entry);
        }

        return entries;
    }

    public static Dictionary createDictionary(InputStream stream) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, "UTF-8");
        Dictionary dict = new Dictionary();

        //Skip the start of document
        parser.next();
        parser.require(XmlPullParser.START_TAG, ns, "volume-metadata-files");
        parser.next();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            } else {
                String name = parser.getName();
                Log.d(TAG, parser.getName());
                // Starts by looking for the entry tag
                if (name.equals("authors")) {
                    dict.setAuthors(parser.getText());
                    skip(parser);
                } else if (name.equals(("cdm-elements"))) {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        String xPath = "";
                        if ((xPath = parser.getAttributeValue(null, "xpath")) != null) {
                            dict.getElements().put(parser.getName(), xPath);
                            Log.d(TAG, (String) dict.getElements().get(parser.getName()));
                            parser.next();
                        }
                    }
                } else {
                    Log.d(TAG, "HEYHEYEHHH");
                    skip(parser);
                }
            }
        }
        return dict;
    }
}
