package jibiki.fr.shishito.Util;

import android.util.Log;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jibiki.fr.shishito.Models.Example;
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

    public static ArrayList<Example> parseExamples(NodeList exList, XPath xPath, Volume volume) throws XPathExpressionException {
        ArrayList<Example> examples = new ArrayList<Example>();
        for (int i = 0; i < exList.getLength(); i++) {
            Element element = (Element) exList.item(i);
            String xpath = adjustXpath("cesselin-example-hiragana", volume);
            Example example = new Example();
            example.setHiragana(xPath.evaluate(xpath, element));
            xpath = adjustXpath("cesselin-example-romaji", volume);
            example.setRomanji(xPath.evaluate(xpath, element));
            xpath = adjustXpath("cesselin-vedette-jpn-match", volume);
            example.setRomanji(xPath.evaluate(xpath, element));
        }

        return examples;
    }

    public static ArrayList<ListEntry> parseEntryList(InputStream stream, Volume volume) throws IOException, XmlPullParserException, XPathExpressionException, SAXException, ParserConfigurationException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        String string = writer.toString();
        string = replaceTags(string, volume.getOldNewTagMap(), volume.getNewOldTagMap());
        //Log.d(TAG, "replacedString:"+string);

        NamespaceContext context = new NamespaceContextMap(
                "d", "http://www-clips.imag.fr/geta/services/dml",
                "xslt", "http://bar",
                "def", "http://def");

        org.xml.sax.InputSource source = new org.xml.sax.InputSource(new java.io.StringReader(string));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(source);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(context);
        ArrayList<ListEntry> entries = new ArrayList<ListEntry>();
        String xpath = adjustXpath("cdm-entry-api", volume);
        NodeList shows = (NodeList) xPath.evaluate(xpath, document, XPathConstants.NODESET);
        for (int i = 0; i < shows.getLength(); i++) {
            Element show = (Element) shows.item(i);
            ListEntry entry = new ListEntry();
            xpath = adjustXpath("cdm-headword", volume);
            entry.setKanji(xPath.evaluate(xpath, show));
            NodeList nodes = (NodeList) xPath.evaluate(xpath, show, XPathConstants.NODESET);
            Node hwjpn = (Node) nodes.item(0);
            if (hwjpn != null) {
                Log.d(TAG, "hwjpn xpath:" + getXpathForNode(hwjpn, volume));
            }
            xpath = adjustXpath("cesselin-writing-display", volume);
            String writing = xPath.evaluate(xpath, show);
            if (writing == null || writing.equals("")) {
                xpath = adjustXpath("cdm-writing", volume);
                writing = xPath.evaluate(xpath, show);
            }
            entry.setRomanji(writing);
            xpath = adjustXpath("cdm-reading", volume);
            entry.setHiragana(xPath.evaluate(xpath, show));
            xpath = adjustXpath("cdm-definition", volume);
            entry.setDefinition(xPath.evaluate(xpath, show));
            xpath = adjustXpath("cdm-pos", volume);
            entry.setGram(xPath.evaluate(xpath, show));

            //xpath = adjustXpath("cdm-example", volume);
            //NodeList exNodes = (NodeList) xPath.evaluate(xpath, show, XPathConstants.NODESET);
            //entry.setExamples(parseExamples(exNodes, xPath, volume));

            entries.add(entry);
            Log.d(TAG, entry.getRomanji());

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
                //Log.d(TAG, parser.getName());
                // Starts by looking for the entry tag
                if (name.equals("authors")) {
                    volume.setAuthors(parser.getText());
                    skip(parser);
                } else if (name.equals(("cdm-elements"))) {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        String xPath = "";
                        if ((xPath = parser.getAttributeValue(null, "xpath")) != null) {
                            volume.getElements().put(parser.getName(), xPath);
                            Log.d(TAG, parser.getName() + ": " + (String) volume.getElements().get(parser.getName()));
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
            Log.d(TAG, "old:" + oldTagname + " new:" + newTagname + " pref:" + prefix);
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

    private static String getXpathForNode(Node theNode, Volume aVolume) {
        String resXpath = "/"+getFullXPath(theNode);
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
}
