/* Copyright (C) 2016 Thibaut Le Guilly et Mathieu Mangeot
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package jibiki.fr.shishito.Util;

import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.GramBlock;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;

/**
 * Created by tibo on 17/09/15.
 * A utility class for XML related functionality.
 */
public final class XMLUtils {

    private static final String ns = null;

    @SuppressWarnings("unused")
    private static final String TAG = XMLUtils.class.getSimpleName();

    private static final NamespaceContext myNamespaceContext = new NamespaceContextMap(
            "d", "http://www-clips.imag.fr/geta/services/dml",
            "xslt", "http://bar",
            "def", "http://def");

    private static final XPathFactory myXPathFactory = XPathFactory.newInstance();


    private static RomajiComparator myRomajiComparator = new RomajiComparator();

    private static java.util.regex.Pattern regexTags = java.util.regex.Pattern.compile("<([^/\\s\\\\?>:]+:)?([^/\\s\\?:>]+)", java.util.regex.Pattern.DOTALL);

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

    private static ListEntry parseEntryStream(InputStream stream, Volume volume) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        Document document = prepareDocumentFromStream(stream, volume);
        XPath xPath = getNewXPath();
        return parseListEntry(xPath, document, volume);
    }

    private static GramBlock parseGramBlock(XPath xPath, String xpath, Element block, Volume volume) throws XPathExpressionException {
        String posPath = adjustXpath("cdm-pos", volume).split("\\|")[0].substring(xpath.length());
        String[] defs = adjustXpath("cdm-definition", volume).split("\\|");
        String sens = adjustXpath("cdm-sense", volume).substring(xpath.length());
        defs[0] = defs[0].substring(xpath.length() + sens.length());
        defs[1] = defs[1].replace(" ", "").substring(xpath.length() + sens.length());

        GramBlock gb = new GramBlock(block);
        gb.setGram(xPath.evaluate("." + posPath, block));
        NodeList sensList = (NodeList) xPath.evaluate("." + sens, block, XPathConstants.NODESET);
        for (int j = 0; j < sensList.getLength(); j++) {
            Element sensEl = (Element) sensList.item(j);
            String defxpath = "." + defs[0].replace("/text()", "");
            //                Log.d(TAG, "def text xpath:" + defxpath);
            Object result = xPath.evaluate(defxpath, sensEl, XPathConstants.NODESET);
            NodeList defNodes = (NodeList) result;
            Node defNode = defNodes.item(0);
            String defResult = getStringFromNode(defNode);
            //               Log.d(TAG, "Def French XML:" + defResult);
            //               Log.d(TAG, "en tag:" + volume.getOldNewTagMap().get("en"));
            if (!TextUtils.isEmpty(defResult) && defResult.contains("<" + volume.getOldNewTagMap().get("en") + ">")) {
                defResult = "<font color=" + ViewUtil.colorEnglish + ">" + defResult + "</font>";
            } else {
                defResult = "<font color=" + ViewUtil.colorFrench + ">" + defResult + "</font>";
            }
            //               Log.d(TAG, "Def French XML end:" + defResult);
            gb.addSens(defResult);
        }
        return gb;
    }

    private static Example parseExample(XPath xPath, String xpath, Element exEl, Volume volume) throws XPathExpressionException {
        String french = adjustXpath("cdm-example-fra", volume).substring(xpath.length());
        french = "." + french.replace("/text()", "");
        String kanji = adjustXpath("cdm-example-jpn", volume).substring(xpath.length());
        kanji = "." + kanji.replace("/text()", "");
        String hiragana = adjustXpath("cesselin-example-hiragana", volume).substring(xpath.length()).replace("/text()", "");
        String romaji = adjustXpath("cesselin-example-romaji", volume).substring(xpath.length()).replace("/text()", "");
        hiragana = "." + hiragana;
        romaji = "." + romaji;
        Example example = new Example();
//        Log.d(TAG, "french xpath: " + french);
        Object result = xPath.evaluate(french, exEl, XPathConstants.NODESET);
        NodeList frenchNodes = (NodeList) result;
        Node frenchNode = frenchNodes.item(0);
        String frenchResult = getStringFromNode(frenchNode);
        //  on vire la balise racine
        if (!TextUtils.isEmpty(frenchResult)) {
            frenchResult = frenchResult.replaceFirst("^<[^>]+>", "");
            frenchResult = frenchResult.replaceFirst("</[^>]+>$", "");
            //  on remplace la balise pb par une couleur de font spéciale
//            Log.d(TAG, "Pb tag: " + "<" + volume.getOldNewTagMap().get("pb") + ">");
            frenchResult = frenchResult.replaceAll("<" + volume.getOldNewTagMap().get("pb") + ">", "<b><font color=" + ViewUtil.colorPbFrench + ">");
            frenchResult = frenchResult.replaceAll("</" + volume.getOldNewTagMap().get("pb") + ">", "</font></b>");
//            frenchResult = "<font color=" + ViewUtil.colorFrench + ">" + frenchResult + "</font>";
            example.setFrench(frenchResult);
        }

        Object resultKanji = xPath.evaluate(kanji, exEl, XPathConstants.NODESET);
        NodeList kanjiNodes = (NodeList) resultKanji;
        Node kanjiNode = kanjiNodes.item(0);
        String kanjiResult = getStringFromNode(kanjiNode);
        //  on vire la balise racine
        if (!TextUtils.isEmpty(kanjiResult)) {
            kanjiResult = kanjiResult.replaceFirst("^<[^>]+>", "");
            kanjiResult = kanjiResult.replaceFirst("</[^>]+>$", "");

            //  en attendant de trouver un moyen d'afficher le furigana, on le vire...
            String rtRegexp = "<" + volume.getOldNewTagMap().get("rt") + ">" + "[^<]+" + "</" + volume.getOldNewTagMap().get("rt") + ">";
            kanjiResult = kanjiResult.replaceAll(rtRegexp, "");
            //  on remplace la balise pb par une couleur de font spéciale
            kanjiResult = kanjiResult.replaceAll("<" + volume.getOldNewTagMap().get("rt") + ">", "</font><font color=" + ViewUtil.colorPbJapanese + "><b>");
            kanjiResult = kanjiResult.replaceAll("</" + volume.getOldNewTagMap().get("pb") + ">", "</b></font><font color=" + ViewUtil.colorJapanese + ">");
            // on pourrait mettre les vedettes en gras, comme sur le site Web...
            kanjiResult = kanjiResult.replaceAll("<" + volume.getOldNewTagMap().get("vj") + ">", "<b>");
            kanjiResult = kanjiResult.replaceAll("</" + volume.getOldNewTagMap().get("vj") + ">", "</b>");
            kanjiResult = "<font color=" + ViewUtil.colorJapanese + ">" + kanjiResult + "</font>";
        }
        example.setKanji(kanjiResult);
        example.setRomaji(xPath.evaluate("string(" + romaji + ")", exEl));
        example.setHiragana(xPath.evaluate("string(" + hiragana + ")", exEl));
        return example;
    }

    private static ListEntry parseListEntry(XPath xPath, Node el, Volume volume) throws XPathExpressionException {
        String xpath;
        ListEntry entry = new ListEntry(el, volume);
        xpath = adjustXpath("cdm-headword", volume);
        NodeList nodes = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            entry.setKanjiNode(nodes.item(0));
        }
        xpath = adjustXpath("cesselin-writing-display", volume);
        entry.setRomajiDisplay(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cdm-writing", volume);
        entry.setRomajiSearch(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cdm-reading", volume);
        NodeList hiraganaNodes = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        if (hiraganaNodes.getLength() > 0) {
            entry.setHiraganaNode(hiraganaNodes.item(0));
        }
 /*       xpath = adjustXpath("cdm-gram-block", volume);
        NodeList gramBlocks = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        for (int i = 0; i < gramBlocks.getLength(); i++) {
            Element block = (Element) gramBlocks.item(i);
            GramBlock gb = parseGramBlock(xPath, xpath, block, volume);
            entry.addGramBlock(gb);
        }

        xpath = adjustXpath("cdm-example-block", volume);
        NodeList examples = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        for (int i = 0; i < examples.getLength(); i++) {
            Element exEl = (Element) examples.item(i);
            Example example = parseExample(xPath, xpath, exEl, volume);
            entry.addExample(example);
//            Log.d(TAG, "Example French: " + example.getFrench());
//            Log.d(TAG, "Example KANJI: " + example.getKanji());
        }
*/
        xpath = adjustXpath("cdm-entry-id", volume);
        entry.setEntryId(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cesselin-vedette-jpn-match", volume);
        entry.setVerified(!TextUtils.isEmpty(xPath.evaluate(xpath, el)));
        xpath = testXpath(volume.getElements().get("cdm-volume") + "/d:contribution/@d:contribid", volume);
        entry.setContribId(xPath.evaluate(xpath, el));
        return entry;
    }

    private static Document prepareDocumentFromStream(InputStream stream, Volume volume) throws IOException, ParserConfigurationException, SAXException { ;
        String string = convertStreamToString(stream);
        string = replaceTags(string, volume.getOldNewTagMap(), volume.getNewOldTagMap());
        org.xml.sax.InputSource source = new org.xml.sax.InputSource(new java.io.StringReader(string));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(source);
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    static XPath getNewXPath() {
        XPath xPath = myXPathFactory.newXPath();
        xPath.setNamespaceContext(myNamespaceContext);
        return xPath;
    }

    public static ArrayList<ListEntry> parseEntryList(InputStream stream, Volume volume) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {

        // volume.initializeTagMaps();
        Document document = prepareDocumentFromStream(stream, volume);

        XPath xPath = getNewXPath();

        ArrayList<ListEntry> entries = new ArrayList<>();
        String xpath = adjustXpath("cdm-entry-api", volume);
        NodeList shows = (NodeList) xPath.evaluate(xpath, document, XPathConstants.NODESET);
        for (int i = 0; i < shows.getLength(); i++) {
            Element show = (Element) shows.item(i);
            ListEntry entry = parseListEntry(xPath, show, volume);
            entries.add(entry);
        }
        java.util.Collections.sort(entries, myRomajiComparator);
        return entries;
    }

    public static Volume createVolume(InputStream stream) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, "UTF-8");
        Volume volume = new Volume();

        //Skip the start of document
        parser.next();
        parser.require(XmlPullParser.START_TAG, ns, "volume-metadata-files");
        parser.next();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                String name = parser.getName();
                // Starts by looking for the entry tag
                switch (name) {
                    case "authors":
                        volume.setAuthors(parser.getText());
                        skip(parser);
                        break;
                    case ("cdm-elements"):
                        while (parser.next() != XmlPullParser.END_TAG) {
                            String xPath;
                            if ((xPath = parser.getAttributeValue(null, "xpath")) != null) {
                                String lang;
                                if (parser.getName().equals("cdm-example") && (lang = parser.getAttributeValue(null, "lang")) != null) {
                                    volume.getElements().put(parser.getName() + "-" + lang, xPath);
                                } else {
                                    volume.getElements().put(parser.getName(), xPath);
                                }
                                parser.next();
                            }
                        }
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
        }

        volume.getElements().put("cdm-entry-api", "/d:entry-list/d:entry");
        return volume;
    }

    private static String replaceTags(String oldXml, java.util.HashMap<String, String> theOldNewTagMap, java.util.HashMap<String, String> theNewOldTagMap) {
        String newXml = oldXml;

        java.util.regex.Matcher m = regexTags.matcher(newXml);

        int numtags = 1 + theOldNewTagMap.size();
        while (m.find()) {
            String oldTagname = m.group(2);
            String prefix = m.group(1);
            if (prefix == null) {
                prefix = "";
            }
            String newTagname = theOldNewTagMap.get(oldTagname);
            if (newTagname == null) {
                newTagname = "a" + numtags++;
                theOldNewTagMap.put(oldTagname, newTagname);
                theNewOldTagMap.put(newTagname, oldTagname);
            }
            newXml = newXml.replaceAll("<" + prefix + oldTagname + "\\s", "<" + prefix + newTagname + " ");
            newXml = newXml.replaceAll("<" + prefix + oldTagname + ">", "<" + prefix + newTagname + ">");
            newXml = newXml.replaceAll("<" + prefix + oldTagname + "\\s?/>", "<" + prefix + newTagname + "/>");
            newXml = newXml.replaceAll("</" + prefix + oldTagname + ">", "</" + prefix + newTagname + ">");
        }

        return newXml;
    }

    static String replaceXpathstring(String xpathString, java.util.HashMap<String, String> theTagMap) {
        java.util.regex.Pattern regexXpath = java.util.regex.Pattern.compile("/([^/\\s\\[:]+:)?([^\\[/\\s:]+)", java.util.regex.Pattern.DOTALL);
        String newXpathString = xpathString;
        //Log.d(TAG, "newXpathString:" + newXpathString);
        java.util.regex.Matcher mXpath = regexXpath.matcher(newXpathString);
        while (mXpath.find()) {
            String oldTagname = mXpath.group(2);
            String prefix = mXpath.group(1);
            if (prefix == null) {
                prefix = "";
            }
            String newTagname = theTagMap.get(oldTagname);
//            Log.d(TAG, "old:" + oldTagname + " new:" + newTagname + " pref:" + prefix);
            if (newTagname != null) {
                newXpathString = newXpathString.replaceAll("/" + prefix + oldTagname + "$", "/" + prefix + newTagname);
                newXpathString = newXpathString.replaceAll("/" + prefix + oldTagname + "/", "/" + prefix + newTagname + "/");
                newXpathString = newXpathString.replaceAll("/" + prefix + oldTagname + "\\[", "/" + prefix + newTagname + "\\[");
            }
        }

        //Log.d(TAG, "modifiedXpathString:" + newXpathString);
        return newXpathString;
    }

    static String adjustXpath(String cdmElement, Volume theVolume) {
        String xpath = theVolume.getElements().get(cdmElement);
        xpath = addContributionTagsToXPath(xpath, theVolume);
        if (!xpath.startsWith(".")) {
            xpath = "." + xpath;
        }
        // à tester !
//        xpath.replaceAll("\\s//", " .//");
//        xpath.replaceAll("^//", ".//");
        //Log.d(TAG, "adjustedXpathString:" + xpath);
        xpath = replaceXpathstring(xpath, theVolume.getOldNewTagMap());
        //Log.d(TAG, "replacedXpathstring:" + xpath);
        return xpath;
    }

    public static String addContributionTagsToXPath(String theXPath, Volume oneVolume) {
        String cdmVolumePath = oneVolume.getElements().get("cdm-volume");
        if (theXPath.contains(cdmVolumePath)) {
            theXPath = theXPath.replace(cdmVolumePath, cdmVolumePath + "/d:contribution/d:data");
        }
        return theXPath;
    }

    public static String removeXpathBeforeVolumeTag(String xpathString, Volume theVolume) {
        String cdmVolumePath = theVolume.getElements().get("cdm-volume");
        if (xpathString.contains(cdmVolumePath)) {
            xpathString = xpathString.replaceFirst("^.*?\\" + cdmVolumePath, cdmVolumePath);
        }
        return xpathString;
    }

    private static String testXpath(String xpath, Volume theVolume) {
        String cdmVolumePath = theVolume.getElements().get("cdm-volume");
        //Log.d(TAG, "notadjustedXpathString:" + xpath);
        if (xpath.contains(cdmVolumePath)) {
            xpath = xpath.replace(cdmVolumePath, "." + cdmVolumePath);
        }
        xpath = replaceXpathstring(xpath, theVolume.getOldNewTagMap());
        return xpath;
    }

    public static String getFullXPath(Node n) {
// abort early
        if (null == n)
            return null;

// declarations
        Node parent;
        Stack<Node> hierarchy = new Stack<>();
        StringBuilder buffer = new StringBuilder();

// push element on stack
        hierarchy.push(n);

        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((org.w3c.dom.Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.TEXT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            // push on stack
            hierarchy.push(parent);

            // get parent of parent
            parent = parent.getParentNode();
        }

// construct xpath
        Object obj;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // is this the root element?
                if (buffer.length() == 0) {
                    // root element - simply append element name
                    buffer.append(node.getNodeName());
                } else {
                    // child element - append slash and element name
                    buffer.append("/");
                    buffer.append(node.getNodeName());

                    /*
                    if (node.hasAttributes()) {
                        // see if the element has a name or id attribute
                        if (e.hasAttribute("id")) {
                            // id attribute found - use that
                            buffer.append("[@id='" + e.getAttribute("id") + "']");
                            handled = true;
                        } else if (e.hasAttribute("name")) {
                            // name attribute found - use that
                            buffer.append("[@name='" + e.getAttribute("name") + "']");
                            handled = true;
                        }
                    } */

                    // no known attribute we could use - get sibling index
                    int prev_siblings = 1;
                    Node prev_sibling = node.getPreviousSibling();
                    while (null != prev_sibling) {
                        if (prev_sibling.getNodeType() == node.getNodeType()) {
                            if (prev_sibling.getNodeName().equalsIgnoreCase(
                                    node.getNodeName())) {
                                prev_siblings++;
                            }
                        }
                        prev_sibling = prev_sibling.getPreviousSibling();
                    }
                    buffer.append("[").append(prev_siblings).append("]");

                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                buffer.append("/@");
                buffer.append(node.getNodeName());
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                buffer.append("/text()");
            }
        }
// return buffer
        return buffer.toString();
    }

    public static String getStringFromNode(Node theNode) {
        try {
            DOMSource domSource = new DOMSource(theNode);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
//            Log.e(TAG, "Error converting document to string: " + ex.getMessage());
            return null;
        }
    }

    private static String getTransformedXPath(String xpath, Volume volume) {
        String cdmVolumePath = volume.getElements().get("cdm-volume");

        if (xpath.contains(cdmVolumePath)) {
            xpath = xpath.replace(cdmVolumePath, cdmVolumePath + "/d:contribution/d:data");
        }
        xpath = xpath.replace("/text()", "");

        return xpath;
    }

    public static String getTransformedNumberedXpath(Volume volume, String cdm, String tag, int num) {
        String xpath = volume.getElements().get(cdm);
        xpath = addNum(xpath, tag, num);
        return getTransformedXPath(xpath, volume);
    }

    private static String addNum(String xpath, String tag, int num) {
        return xpath.replace("/" + tag + "/", "/" + tag + "[" + num + "]/");
    }

    public static ListEntry handleListEntryStream(InputStream stream, Volume volume) {
        ListEntry entry = null;
        if (stream != null) {
            try {
                entry = parseEntryStream(stream, volume);
            } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
//                Log.e(TAG, "Error parsing entry stream: " + e.getMessage());
            }
        } else {
            return null;
        }

        return entry;
    }
}
