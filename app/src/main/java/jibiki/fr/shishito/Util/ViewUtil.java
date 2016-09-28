package jibiki.fr.shishito.Util;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import jibiki.fr.shishito.Interfaces.FastEditListener;
import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.R;

public final class ViewUtil {

    /*
    Couleurs sur jibiki.fr :
    romaji : #D45455
    jpn : #BC002D
    français : #002395
    problème sur le japonais (fond orange) : #ffa500
    anglais (fond vert) : #BFFF00
    problème sur le français (fond jaune) : #ffff00

    Avec Android, c'est compliqué de mettre un fond, donc on va pour l'instant utiliser une autre couleur.
    anglais (vert foncé) : #009900
    problème sur le japonais (violet) : #6600ff
    problème sur le français (jaune foncé) : #FFCC00

 */

    static final String colorFrench = "#002395";
    static final String colorRomaji = "#D45455";
    static final String colorJapanese = "#BC002D";
    static final String colorEnglish = "#009900";
    static final String colorPbFrench = "#FFCC00";
    static final String colorPbJapanese = "#6600ff";


    @SuppressWarnings("unused")
    private static final String TAG = ViewUtil.class.getSimpleName();

    private static final String nonEmptyXml = "<{1}.*>{1}.+(<\\/){1}.*>{1}";


    public static void parseAndAddGramBlocksToView(View v, ListEntry entry, Context context, boolean clickable) {
        String blockXpathString = XMLUtils.adjustXpath("cdm-gram-block", entry.getVolume());
        try {
            XPath xPath = XMLUtils.getNewXPath();
            NodeList gramBlocks = (NodeList) xPath.evaluate(blockXpathString, entry.getNode(), XPathConstants.NODESET);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);
            for (int i = 0; i < gramBlocks.getLength(); i++) {
                Element block = (Element) gramBlocks.item(i);
                parseAndAddGramBlockToView(block, v, entry, context, params, clickable);
            }
        } catch (XPathExpressionException ex) {
            Log.e(TAG, "Error xPath: " + ex.getMessage());
        }
    }

    private static void parseAndAddGramBlockToView(Element block, View v, ListEntry entry,
                                                   Context context, LinearLayout.LayoutParams params,
                                                   boolean clickable) {
        try {
            XPath xPath = XMLUtils.getNewXPath();
            String gramblockPath = XMLUtils.adjustXpath("cdm-gram-block", entry.getVolume());
            String posPath = ("." + XMLUtils.adjustXpath("cdm-pos", entry.getVolume()).split("\\|")[0].substring(gramblockPath.length()));
            String[] defPaths = XMLUtils.adjustXpath("cdm-definition", entry.getVolume()).split("\\|");
            String sensPath = XMLUtils.adjustXpath("cdm-sense", entry.getVolume()).substring(gramblockPath.length());
            defPaths[0] = defPaths[0].substring(gramblockPath.length() + sensPath.length());
            defPaths[1] = defPaths[1].replace(" ", "").substring(gramblockPath.length() + sensPath.length());
            sensPath = "." + sensPath;
            String defPath = "." + defPaths[0].replace("/text()", "");

            String gramString = xPath.evaluate(posPath, block);
            TextView gramView = new TextView(context);
            TextViewCompat.setTextAppearance(gramView, android.R.style.TextAppearance_Small);
            gramView.setLayoutParams(params);
            if (gramString != null) {
                gramView.setText(context.getString(R.string.in_bracket, gramString));
            }
            ((LinearLayout) v).addView(gramView);

            NodeList sensList = (NodeList) xPath.evaluate(sensPath, block, XPathConstants.NODESET);
            for (int j = 0; j < sensList.getLength(); j++) {
                Element sensEl = (Element) sensList.item(j);
                NodeList defNodes = (NodeList) xPath.evaluate(defPath, sensEl, XPathConstants.NODESET);
                Node defNode = defNodes.item(0);
                TextView senseView = new TextView(context);
                senseView.setLayoutParams(params);
                TextViewCompat.setTextAppearance(senseView, android.R.style.TextAppearance_Medium);
                if (sensList.getLength() > 1) {
                    senseView.append((j + 1) + ". ");
                }
                parseAndAddSenseToView(defNode, entry, context, senseView, clickable);
                ((LinearLayout) v).addView(senseView);
            }
        } catch (XPathExpressionException ex) {
            Log.e(TAG, "Error xPath: " + ex.getMessage());
        }
    }

    private static void parseAndAddSenseToView(Node senseNode, ListEntry entry, Context context, TextView senseView, boolean clickable) {
        String defResult = XMLUtils.getStringFromNode(senseNode);
        if (defResult != null) {
            defResult = defResult.replaceFirst("^<[^>]+>", "");
            defResult = defResult.replaceFirst("</[^>]+>$", "");
            //  on remplace la balise pb par une couleur de font spéciale
            defResult = defResult.replaceAll("<" + entry.getVolume().getOldNewTagMap().get("pb") + ">", "</font><b><font color=" + ViewUtil.colorPbFrench + ">");
            defResult = defResult.replaceAll("</" + entry.getVolume().getOldNewTagMap().get("pb") + ">", "</font></b><font color=" + ViewUtil.colorFrench + ">");

            if (!TextUtils.isEmpty(defResult) && defResult.contains("<" + entry.getVolume().getOldNewTagMap().get("en") + ">")) {
                defResult = "<font color=" + ViewUtil.colorEnglish + ">" + defResult + "</font>";
            } else {
                defResult = "<font color=" + ViewUtil.colorFrench + ">" + defResult + "</font>";
            }
            String xpathPointer = "/" + XMLUtils.getFullXPath(senseNode);
            xpathPointer = XMLUtils.replaceXpathstring(xpathPointer, entry.getVolume().getNewOldTagMap());
            xpathPointer = XMLUtils.removeXpathBeforeVolumeTag(xpathPointer, entry.getVolume());
            appendClickSpannable(defResult,
                    context, entry, "sense", xpathPointer, senseView, clickable);
        }
    }


    private static void appendClickSpannable(String input,
                                             Context context, ListEntry entry,
                                             final String title,
                                             final String xpath,
                                             TextView tv, boolean clickable) {
        SpannableString ss = new SpannableString(Html.fromHtml(input));
        if (clickable) {
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            final String contribId = entry.getContribId();
            final String word = removeFancy(input);
            if (context instanceof FastEditListener) {
                final FastEditListener fel = (FastEditListener) context;
                ClickableSpan cs = new ClickableSpan() {
                    String s = word;

                    @Override
                    public void onClick(View widget) {
                        if (fel.isLoggedIn()) {
                            fel.putFastEdit(contribId, xpath, s, title);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {// override updateDrawState
                        ds.setUnderlineText(false); // set to false to remove underline
                    }
                };
                ss.setSpan(cs, 0, word.length(), 0);
            }
        }
        tv.append(ss);
    }


    public static void parseAndAddExamplesToView(View v, ListEntry entry, Context context,
                                                 boolean clickable) {
        String blockXpathString = XMLUtils.adjustXpath("cdm-example-block", entry.getVolume());
        try {

            XPath xPath = XMLUtils.getNewXPath();
            NodeList exampleBlocks = (NodeList) xPath.evaluate(blockXpathString, entry.getNode(), XPathConstants.NODESET);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 8);
            if (exampleBlocks.getLength() > 0) {
                TextView exTitle = new TextView(context);
                TextViewCompat.setTextAppearance(exTitle, android.R.style.TextAppearance_Large);
                exTitle.setText(R.string.example);
                ((LinearLayout) v).addView(exTitle);
            }
            for (int i = 0; i < exampleBlocks.getLength(); i++) {
                Element block = (Element) exampleBlocks.item(i);
                TextView exampleView = parseAndCreateExampleView(block, entry, context, blockXpathString, clickable);
                ((LinearLayout) v).addView(exampleView);
            }
        } catch (XPathExpressionException ex) {
            Log.e(TAG, "Error xPath: " + ex.getMessage());
        }
    }

    private static TextView parseAndCreateExampleView(Element exampleNode, ListEntry entry,
                                                      Context context, String blockXpathString,
                                                      boolean clickable) {
        TextView exView = new TextView(context);
        exView.setLineSpacing(8, 1);
        exView.append("▪ ");
        TextViewCompat.setTextAppearance(exView, android.R.style.TextAppearance_Medium);
        try {

            XPath xPath = XMLUtils.getNewXPath();

            String kanjiPath = XMLUtils.adjustXpath("cdm-example-jpn", entry.getVolume()).substring(blockXpathString.length());
            kanjiPath = "." + kanjiPath.replace("/text()", "");
            NodeList kanjiNodes = (NodeList) xPath.evaluate(kanjiPath, exampleNode, XPathConstants.NODESET);
            Node kanjiNode = kanjiNodes.item(0);
            parseAndAddExampleJpnToView(kanjiNode, entry, context, exView, clickable);


            String romajiPath = XMLUtils.adjustXpath("cesselin-example-romaji", entry.getVolume()).substring(blockXpathString.length());
            romajiPath = "." + romajiPath.replace("/text()", "");
            NodeList romajiNodes = (NodeList) xPath.evaluate(romajiPath, exampleNode, XPathConstants.NODESET);
            Node romajiNode = romajiNodes.item(0);
            exView.append(Html.fromHtml("\u00A0<font color=" + colorRomaji + ">(</font>"));
            parseAndAddExampleRomajiToView(romajiNode, entry, context, exView, clickable);
            exView.append(Html.fromHtml("<font color=" + colorRomaji + ">)</font> "));

            String frenchPath = XMLUtils.adjustXpath("cdm-example-fra", entry.getVolume()).substring(blockXpathString.length());
            frenchPath = "." + frenchPath.replace("/text()", "");
            NodeList frenchNodes = (NodeList) xPath.evaluate(frenchPath, exampleNode, XPathConstants.NODESET);
            for (int i = 0; i < frenchNodes.getLength(); i++) {
                Element frenchNode = (Element) frenchNodes.item(i);
                if (frenchNodes.getLength() > 1) {
                    exView.append(" [" + (i + 1) + "] ");
                }
                parseAndAddExampleFrenchToView(frenchNode, entry, context, exView, clickable);
            }
        } catch (XPathExpressionException ex) {
            Log.e(TAG, "Error xPath: " + ex.getMessage());
        }
        exView.setTextColor(Color.parseColor(colorFrench));
        return exView;
    }

    private static void parseAndAddExampleJpnToView(Node exampleJpnNode, ListEntry entry,
                                                    Context context, TextView exView, boolean clickable) {
        String kanjiResult = XMLUtils.getStringFromNode(exampleJpnNode);
        if (!TextUtils.isEmpty(kanjiResult) && kanjiResult.matches(nonEmptyXml)) {

            kanjiResult = kanjiResult.replaceFirst("^<[^>]+>", "");
            kanjiResult = kanjiResult.replaceFirst("</[^>]+>$", "");
            //  en attendant de trouver un moyen d'afficher le furigana, on le vire...
            String rtRegexp = "<" + entry.getVolume().getOldNewTagMap().get("rt") + ">" + "[^<]+" + "</" + entry.getVolume().getOldNewTagMap().get("rt") + ">";
            kanjiResult = kanjiResult.replaceAll(rtRegexp, "");
            //  on remplace la balise pb par une couleur de font spéciale
            kanjiResult = kanjiResult.replaceAll("<" + entry.getVolume().getOldNewTagMap().get("pb") + ">", "</font><font color=" + ViewUtil.colorPbJapanese + "><b>");
            kanjiResult = kanjiResult.replaceAll("</" + entry.getVolume().getOldNewTagMap().get("pb") + ">", "</b></font><font color=" + ViewUtil.colorJapanese + ">");
            // on pourrait mettre les vedettes en gras, comme sur le site Web...
            kanjiResult = kanjiResult.replaceAll("<" + entry.getVolume().getOldNewTagMap().get("vj") + ">", "<b>");
            kanjiResult = kanjiResult.replaceAll("</" + entry.getVolume().getOldNewTagMap().get("vj") + ">", "</b>");
            kanjiResult = "<font color=" + ViewUtil.colorJapanese + ">" + kanjiResult + "</font>";

            String xpathPointer = "/" + XMLUtils.getFullXPath(exampleJpnNode);
            xpathPointer = XMLUtils.replaceXpathstring(xpathPointer, entry.getVolume().getNewOldTagMap());
            xpathPointer = XMLUtils.removeXpathBeforeVolumeTag(xpathPointer, entry.getVolume());
            appendClickSpannable(kanjiResult, context, entry,
                    "exemple kanji", xpathPointer, exView, clickable);
        }
    }


    private static void parseAndAddExampleRomajiToView(Node exampleRomajiNode, ListEntry entry, Context context,
                                                       TextView exView, boolean clickable) {
        String romajiResult = XMLUtils.getStringFromNode(exampleRomajiNode);
        if (!TextUtils.isEmpty(romajiResult) && romajiResult.matches(nonEmptyXml)) {
            String xpathPointer = "/" + XMLUtils.getFullXPath(exampleRomajiNode);
            xpathPointer = XMLUtils.replaceXpathstring(xpathPointer, entry.getVolume().getNewOldTagMap());
            xpathPointer = XMLUtils.removeXpathBeforeVolumeTag(xpathPointer, entry.getVolume());

            romajiResult = "<font color=" + colorRomaji + ">" + romajiResult + "</font>";
            appendClickSpannable(romajiResult,
                    context, entry, "exemple romaji", xpathPointer, exView, clickable);
        }

    }

    private static void parseAndAddExampleFrenchToView(Node exampleFrenchNode, ListEntry entry,
                                                       Context context, TextView exView,
                                                       boolean clickable) {
        String frenchResult = XMLUtils.getStringFromNode(exampleFrenchNode);
        if (!TextUtils.isEmpty(frenchResult) && frenchResult.matches(nonEmptyXml)) {

            frenchResult = frenchResult.replaceFirst("^<[^>]+>", "");
            frenchResult = frenchResult.replaceFirst("</[^>]+>$", "");
            //  on remplace la balise pb par une couleur de font spéciale
//            Log.d(TAG, "Pb tag: " + "<" + volume.getOldNewTagMap().get("pb") + ">");
            frenchResult = frenchResult.replaceAll("<" + entry.getVolume().getOldNewTagMap().get("pb") + ">", "<b><font color=" + ViewUtil.colorPbFrench + ">");
            frenchResult = frenchResult.replaceAll("</" + entry.getVolume().getOldNewTagMap().get("pb") + ">", "</font></b>");

            String xpathPointer = "/" + XMLUtils.getFullXPath(exampleFrenchNode);
            xpathPointer = XMLUtils.replaceXpathstring(xpathPointer, entry.getVolume().getNewOldTagMap());
            xpathPointer = XMLUtils.removeXpathBeforeVolumeTag(xpathPointer, entry.getVolume());

            appendClickSpannable(frenchResult, context, entry,
                    "exemple français", xpathPointer, exView, clickable);
        }
    }


    public static void addExamplesToView(View v, ListEntry entry, Context context,
                                         boolean canEdit) {
        ArrayList<Example> examples = entry.getExamples();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        if (examples.size() > 0) {
            TextView exTitle = new TextView(context);
            TextViewCompat.setTextAppearance(exTitle, android.R.style.TextAppearance_Large);
            exTitle.setText(R.string.example);
            ((LinearLayout) v).addView(exTitle);
        }
        int i = 1;
        for (Example ex : examples) {
            TextView exView = new TextView(context);
            exView.setLineSpacing(8, 1);
            TextViewCompat.setTextAppearance(exView, android.R.style.TextAppearance_Medium);

            String xpath = XMLUtils.getTransformedNumberedXpath(entry.getVolume(), "cdm-example-jpn", "exemple", i);
            appendClickSpannable(ex.getKanji(), context, entry,
                    "example kanji", xpath, exView, canEdit);
            xpath = XMLUtils.getTransformedNumberedXpath(entry.getVolume(), "cesselin-example-romaji", "exemple", i);
            appendClickSpannable("<font color=" + colorRomaji + ">(" + ex.getRomaji() + ")</font>",
                    context, entry, "example romaji", xpath, exView, canEdit);
            exView.append("  ");
            xpath = XMLUtils.getTransformedNumberedXpath(entry.getVolume(), "cdm-example-fra", "exemple", i);
            appendClickSpannable(ex.getFrench(), context, entry, "example français",
                    xpath, exView, canEdit);
            exView.setTextColor(Color.parseColor(colorFrench));
            ((LinearLayout) v).addView(exView);
            i++;
        }
    }

    public static void addVedette(TextView v, ListEntry entry, Context context, boolean clickable) {
        String romaji = entry.getRomajiDisplay();
        if (TextUtils.isEmpty(romaji)) {
            romaji = entry.getRomajiSearch();
        }
        Node kanjiNode = entry.getKanjiNode();
        String kanji = XMLUtils.getStringFromNode(kanjiNode);
        if (entry.isVerified()) {
            kanji = "<font color=" + colorJapanese + ">" + kanji + "</font>";
            v.append(Html.fromHtml(kanji));
        } else {
            kanji = "<font color=" + colorPbJapanese + ">" + kanji + "</font>";
            String xpathPointer = "/" + XMLUtils.getFullXPath(kanjiNode);
            xpathPointer = XMLUtils.replaceXpathstring(xpathPointer, entry.getVolume().getNewOldTagMap());
            xpathPointer = XMLUtils.removeXpathBeforeVolumeTag(xpathPointer, entry.getVolume());
            appendClickSpannable(kanji, context, entry, "vedette Kanji", xpathPointer, v, clickable);
        }
        String hiraganaString = XMLUtils.getStringFromNode(entry.getHiraganaNode());
        String vText = "   <font color=" + colorJapanese + ">【" + hiraganaString + "】</font>   " +
                "   <font color=" + colorRomaji + ">(" + romaji + ")</font>";
        v.append(Html.fromHtml(vText));
    }

    public static void addVerified(View v, ListEntry entry) {
        if (!entry.isVerified()) {
            TextView verif = new TextView(v.getContext());
            verif.setText(R.string.verif);
            ((LinearLayout) v).addView(verif);
        }
    }

    public static String removeFancy(String input) {
        if (!TextUtils.isEmpty(input)) {
            return input.replaceAll("<(.*?)>", "");
        }
        return input;
    }

    public static String normalizeQueryString(String string) {
        string = string.replace(" ", "");
        return string;
    }

    public static String replace_macron(String string) {
        string = string.replace("â", "ā");
        string = string.replace("ê", "ē");
        string = string.replace("î", "ī");
        string = string.replace("ô", "ō");
        string = string.replace("û", "ū");
        string = string.replace("a" + "̄", "ā");
        string = string.replace("e" + "̄", "ē");
        string = string.replace("i" + "̄", "ī");
        string = string.replace("o" + "̄", "ō");
        string = string.replace("u" + "̄", "ū");
        return string;
    }

    public static String to_hepburn(String string) {
        string = string.replace("si", "shi");
        string = string.replace("ti", "chi");
        string = string.replace("tu", "tsu");
        string = string.replaceAll("([^s])hu", "$1fu");
        string = string.replaceFirst("^hu", "fu");
        string = string.replace("zi", "ji");
        string = string.replace("di", "ji");
        string = string.replace("du", "zu");
        string = string.replace("sya", "sha");
        string = string.replace("tya", "cha");
        string = string.replace("zya", "ja");
        string = string.replace("syu", "shu");
        string = string.replace("tyu", "chu");
        string = string.replace("zyu", "ju");
        string = string.replace("syo", "sho");
        string = string.replace("tyo", "cho");
        string = string.replace("zyo", "jo");
        return string;
    }
}