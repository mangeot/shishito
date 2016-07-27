package jibiki.fr.shishito.Util;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
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
 */
public final class XMLUtils {

    private static final String ns = null;
    private static final String TAG = XMLUtils.class.getSimpleName();
    private static RomajiComparator myRomajiComparator = new RomajiComparator();

    protected static java.util.regex.Pattern regexXpath = java.util.regex.Pattern.compile("/([^\\/\\s\\[:]+:)?([^\\[\\/\\s:]+)", java.util.regex.Pattern.DOTALL);
    protected static java.util.regex.Pattern regexTags = java.util.regex.Pattern.compile("<([^\\/\\s\\\\?>:]+:)?([^\\/\\s\\?:>]+)", java.util.regex.Pattern.DOTALL);

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

    public static ListEntry parseEntryStream(InputStream stream, Volume volume) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        Document document = prepareDocumentFromStream(stream, volume);
        XPath xPath = getNewXPath();
        return parseListEntry(xPath, document, volume);
    }

    private static ListEntry parseListEntry(XPath xPath, Node el, Volume volume) throws XPathExpressionException {
/*
    Couleurs sur jibiki.fr :
    romaji : #D45455
    jpn : #BC002D
    français : #002395
    problème sur le japonais (fond orange) : #ffa500
    anglais (fond vert) : #BFFF00
    problème sur le français (fond jaune) : #ffff00

 */
        String xpath = "";
        ListEntry entry = new ListEntry();
        xpath = adjustXpath("cdm-headword", volume);
        entry.setKanji(xPath.evaluate(xpath, el));
        NodeList nodes = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        Node hwjpn = (Node) nodes.item(0);
//        if (hwjpn != null) {
//            Log.d(TAG, "hwjpn xpath:" + getXpathForNode(hwjpn, volume));
//        }
        xpath = adjustXpath("cesselin-writing-display", volume);
        entry.setRomajiDisplay(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cdm-writing", volume);
        entry.setRomajiSearch(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cdm-reading", volume);
        entry.setHiragana(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cdm-gram-block", volume);
        String posPath = adjustXpath("cdm-pos", volume).split("\\|")[0].substring(xpath.length());
        String sens = adjustXpath("cdm-sense", volume).substring(xpath.length());
        String[] defs = adjustXpath("cdm-definition", volume).split("\\|");

        defs[0] = defs[0].substring(xpath.length() + sens.length());
        defs[1] = defs[1].replace(" ", "").substring(xpath.length() + sens.length());

        NodeList gramBlocks = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        for (int i = 0; i < gramBlocks.getLength(); i++) {
            Element block = (Element) gramBlocks.item(i);
            GramBlock gb = new GramBlock();
            gb.setGram(xPath.evaluate("." + posPath, block));
            NodeList sensList = (NodeList) xPath.evaluate("." + sens, block, XPathConstants.NODESET);
            for (int j = 0; j < sensList.getLength(); j++) {
                Element sensEl = (Element) sensList.item(j);
/*
                String s = xPath.evaluate("string(." + defs[0].replace("/text()", "") + ")", sensEl);
                if (TextUtils.isEmpty(s)) {
                    s = "<font color=#00e600>" + xPath.evaluate("." + defs[1], sensEl) + "</font>";
                }
*/
                String defxpath = "." + defs[0].replace("/text()","");
                //                Log.d(TAG, "def text xpath:" + defxpath);
                Object result = xPath.evaluate(defxpath, sensEl, XPathConstants.NODESET);
            NodeList defNodes = (NodeList) result;
            Node defNode = defNodes.item(0);
            String defResult = getStringFromNode(defNode);
                //               Log.d(TAG, "Def French XML:" + defResult);
                //               Log.d(TAG, "en tag:" + volume.getOldNewTagMap().get("en"));
                if (defResult.contains("<"+volume.getOldNewTagMap().get("en")+">")) {
                    defResult = defResult.replaceAll("<"+volume.getOldNewTagMap().get("en")+">","<font color=#BFFF00>");
                    defResult = defResult.replaceAll("</"+volume.getOldNewTagMap().get("en")+">","</font>");
                }
                else {
                    defResult = "<font color=#002395>" + defResult + "</font>";
                }
                //               Log.d(TAG, "Def French XML end:" + defResult);
                gb.addSens(defResult);
            }
            entry.addGramBlock(gb);
        }

        xpath = adjustXpath("cdm-example-block", volume);
        String french = adjustXpath("cdm-example", volume).substring(xpath.length());
        String hiragana = adjustXpath("cesselin-example-hiragana", volume).substring(xpath.length());
        String romaji = adjustXpath("cesselin-example-romaji", volume).substring(xpath.length()).replace("/text()", "");

        NodeList examples = (NodeList) xPath.evaluate(xpath, el, XPathConstants.NODESET);
        for (int i = 0; i < examples.getLength(); i++) {
            Element exEl = (Element) examples.item(i);
            Example example = new Example();
            //          example.setFrench(xPath.evaluate("." + french, exEl));
            french = "." + french.replace("/text()","");
            Object result = xPath.evaluate(french, exEl, XPathConstants.NODESET);
            NodeList frenchNodes = (NodeList) result;
            Node frenchNode = frenchNodes.item(0);
            String frenchResult = getStringFromNode(frenchNode);
            Log.d(TAG, "Example French XML:" + frenchResult);
            Log.d(TAG, "French pb tag: " + "<"+volume.getOldNewTagMap().get("pb")+">");
            frenchResult = frenchResult.replaceAll("<"+volume.getOldNewTagMap().get("pb")+">","<font color=#ffff00>");
            frenchResult = frenchResult.replaceAll("</"+volume.getOldNewTagMap().get("pb")+">","</font>");
            // frenchResult = "<font color=#002395>" + frenchResult + "</font>";
            Log.d(TAG, "Example French XML end:" + frenchResult);
            example.setFrench(frenchResult);

            example.setHiragana("<font color=#BC002D>" + xPath.evaluate("." + hiragana, exEl) + "</font>");
            example.setRomaji("<font color=#D45455>" + xPath.evaluate("string(." + romaji + ")", exEl)+ "</font>");
            entry.addExample(example);
        }

        xpath = adjustXpath("cdm-entry-id", volume);
        entry.setEntryId(xPath.evaluate(xpath, el));
        xpath = adjustXpath("cesselin-vedette-jpn-match", volume);
        entry.setVerified(!TextUtils.isEmpty(xPath.evaluate(xpath, el)));
        xpath = testXpath("/volume/d:contribution/@d:contribid", volume);
        entry.setContribId(xPath.evaluate(xpath, el));
        return entry;
    }

    private static Document prepareDocumentFromStream(InputStream stream, Volume volume) throws IOException, ParserConfigurationException, SAXException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        String string = writer.toString();
        string = replaceTags(string, volume.getOldNewTagMap(), volume.getNewOldTagMap());
        org.xml.sax.InputSource source = new org.xml.sax.InputSource(new java.io.StringReader(string));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(source);
    }

    private static XPath getNewXPath() {
        NamespaceContext context = new NamespaceContextMap(
                "d", "http://www-clips.imag.fr/geta/services/dml",
                "xslt", "http://bar",
                "def", "http://def");

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(context);
        return xPath;
    }

    public static ArrayList<ListEntry> parseEntryList(InputStream stream, Volume volume) throws IOException, XmlPullParserException, XPathExpressionException, SAXException, ParserConfigurationException {

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
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, "UTF-8");
        Volume volume = new Volume();

        //Skip the start of document
        parser.next();
        parser.require(XmlPullParser.START_TAG, ns, "volume-metadata-files");
        parser.next();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            } else {
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("authors")) {
                    volume.setAuthors(parser.getText());
                    skip(parser);
                } else if (name.equals(("cdm-elements"))) {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        String xPath = "";
                        if ((xPath = parser.getAttributeValue(null, "xpath")) != null) {
                            volume.getElements().put(parser.getName(), xPath);
                            parser.next();
                        }
                    }
                } else {
                    skip(parser);
                }
            }
        }

        volume.getElements().put("cdm-entry-api", "/d:entry-list/d:entry");
        return volume;
    }

    protected static String replaceTags(String oldXml, java.util.HashMap<String, String> theOldNewTagMap, java.util.HashMap<String, String> theNewOldTagMap) {
        String newXml = oldXml;

        java.util.regex.Matcher m = regexTags.matcher(newXml);

        int numtags = 1;
        while (m.find()) {
            String oldTagname = m.group(2);
            String prefix = m.group(1);
            if (prefix == null) {
                prefix = "";
            }
            String newTagname = theOldNewTagMap.get(oldTagname);
//            Log.d(TAG, "Old: " + oldTagname + " New: " + newTagname);
            if (newTagname == null) {
                newTagname = "a" + numtags++;
                theOldNewTagMap.put(oldTagname, newTagname);
                theNewOldTagMap.put(newTagname, oldTagname);
                //Log.d(TAG, "old:" + oldTagname + " new new:" + newTagname + " pref:" + prefix);
            } else {
                //Log.d(TAG, "old:" + oldTagname + " new found:" + newTagname + " pref:" + prefix);
            }
            newXml = newXml.replaceAll("<" + prefix + oldTagname + "\\s", "<" + prefix + newTagname + " ");
            newXml = newXml.replaceAll("<" + prefix + oldTagname + ">", "<" + prefix + newTagname + ">");
            newXml = newXml.replaceAll("<" + prefix + oldTagname + "\\s?/>", "<" + prefix + newTagname + "/>");
            newXml = newXml.replaceAll("</" + prefix + oldTagname + ">", "</" + prefix + newTagname + ">");
        }

        return newXml;
    }

    protected static String replaceXpathstring(String xpathString, java.util.HashMap<String, String> theTagMap) {
        java.util.regex.Pattern regexXpath = java.util.regex.Pattern.compile("/([^\\/\\s\\[:]+:)?([^\\[\\/\\s:]+)", java.util.regex.Pattern.DOTALL);
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

    protected static String adjustXpath(String cdmElement, Volume theVolume) {
        String xpath = ((String) theVolume.getElements().get(cdmElement));
        String cdmVolumePath = (String) theVolume.getElements().get("cdm-volume");
        //Log.d(TAG, "notadjustedXpathString:" + xpath);
        if (xpath.contains(cdmVolumePath)) {
            xpath = xpath.replace(cdmVolumePath, "." + cdmVolumePath + "/d:contribution/d:data");
        }
        // à tester !
        xpath.replaceAll("\\s//", " .//");
        xpath.replaceAll("^//", ".//");
        //Log.d(TAG, "adjustedXpathString:" + xpath);
        xpath = replaceXpathstring(xpath, theVolume.getOldNewTagMap());
        //Log.d(TAG, "replacedXpathstring:" + xpath);
        return xpath;
    }

    protected static String testXpath(String xpath, Volume theVolume) {
        String cdmVolumePath = (String) theVolume.getElements().get("cdm-volume");
        //Log.d(TAG, "notadjustedXpathString:" + xpath);
        if (xpath.contains(cdmVolumePath)) {
            xpath = xpath.replace(cdmVolumePath, "." + cdmVolumePath);
        }
        // à tester !
        xpath.replaceAll("\\s//", " .//");
        xpath.replaceAll("^//", ".//");
        //Log.d(TAG, "adjustedXpathString:" + xpath);
        xpath = replaceXpathstring(xpath, theVolume.getOldNewTagMap());
        //Log.d(TAG, "replacedXpathstring:" + xpath);
        return xpath;
    }

    private static String getXpathForNode(Node theNode, Volume aVolume) {
        String resXpath = "/" + getFullXPath(theNode);
        resXpath = replaceXpathstring(resXpath, aVolume.getNewOldTagMap());
        resXpath = resXpath.replaceFirst("/d:entry-list/d:entry\\[[0-9]+\\]", "");
        //resXpath += "/text()";
        return resXpath;
    }

    protected static String getFullXPath(Node n) {
// abort early
        if (null == n)
            return null;

// declarations
        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();
        StringBuffer buffer = new StringBuffer();

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
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

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

                    if (!handled) {
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
                        buffer.append("[" + prev_siblings + "]");
                    }
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

    private static String getStringFromNode(Node theNode) {
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
            Log.e(TAG, "Error converting document to string: " + ex.getMessage());
            return null;
        }
    }
}