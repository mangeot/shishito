package jibiki.fr.shishito.Util;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tibo on 17/09/15.
 */
public final class XMLUtils {

    private static final String ns = null;
    private static final String TAG = XMLUtils.class.getSimpleName();


    private XMLUtils(){}

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
        while(parser.next()!=XmlPullParser.END_TAG){
            if(parser.getName().equals("criteria")){
                if(parser.next() == XmlPullParser.TEXT) {
                    entry = parser.getText();
                }
                //Move to end of criteria tag
                parser.next();
                parser.require(XmlPullParser.END_TAG, ns, "criteria");
            }else{
                skip(parser);
            }
        }
        return entry;
    }

    public static ArrayList<String> parseEntryList(InputStream stream) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, "UTF-8");
        ArrayList<String> entries = new ArrayList<>();

        //Skip the start of document
        parser.next();
        parser.require(XmlPullParser.START_TAG, ns, "entry-list");
        Log.d(TAG, parser.getName());
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.d(TAG, parser.getName());
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            }
        }
        return entries;
    }
}
