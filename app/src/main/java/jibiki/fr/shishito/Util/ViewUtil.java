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

import java.util.ArrayList;

import jibiki.fr.shishito.Interfaces.FastEditListener;
import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.GramBlock;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
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

    public static final String colorFrench = "#002395";
    public static final String colorRomaji = "#D45455";
    public static final String colorJapanese = "#BC002D";
    public static final String colorEnglish = "#009900";
    public static final String colorPbFrench = "#FFCC00";
    public static final String colorPbJapanese = "#6600ff";


    private static final String TAG = ViewUtil.class.getSimpleName();


    public static void addGramBlocksToView(View v, ListEntry entry, Context context,
                                           boolean canEdit, Volume volume) {
        ArrayList<GramBlock> gramBlocks = entry.getGramBlocks();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 5, 0, 5);
        int count = 1;
        for (GramBlock gb : gramBlocks) {
            TextView gram = new TextView(context);
            TextViewCompat.setTextAppearance(gram, android.R.style.TextAppearance_Small);
            gram.setLayoutParams(params);
            if (gb.getGram() != null) {
                gram.setText(context.getString(R.string.in_bracket, gb.getGram()));
            }
            ((LinearLayout) v).addView(gram);
            int i = 1;
            for (String s : gb.getSens()) {
                TextView senseView = new TextView(context);
                senseView.setLayoutParams(params);
                senseView.append(i + ". ");
                appendClickSpannable(s, canEdit,
                        context, entry, "sense", "cdm-definition", count, senseView, volume);
                TextViewCompat.setTextAppearance(senseView, android.R.style.TextAppearance_Medium);
                ((LinearLayout) v).addView(senseView);
                count++;
                i++;
            }
        }
    }

    private static void appendClickSpannable(String input, boolean clickable,
                                             Context context, ListEntry entry,
                                             final String title,
                                             String cdm, int num,
                                             TextView tv, Volume volume) {
        SpannableString ss = new SpannableString(Html.fromHtml(input));
        if (clickable) {
            final String xpath = XMLUtils.getTransformedXPath(cdm, num, volume);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            final String contribId = entry.getContribId();
            final String word = removeFancy(input);
            if (context instanceof FastEditListener) {
                final FastEditListener fel = (FastEditListener) context;
                ClickableSpan cs = new ClickableSpan() {
                    String s = word;

                    @Override
                    public void onClick(View widget) {
                        fel.putFastEdit(contribId, xpath, s, title);
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

    public static void addExamplesToView(View v, ListEntry entry, Context context,
                                         boolean canEdit, Volume volume) {
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

            appendClickSpannable(ex.getKanji(), canEdit, context, entry,
                    "example kanji", "cdm-example-jpn", i, exView, volume);
            appendClickSpannable("<font color=" + colorRomaji + ">(" + ex.getRomaji() + ")</font>", canEdit,
                    context, entry, "example romaji", "cesselin-example-romaji", i, exView, volume);
            exView.append("  ");
            appendClickSpannable(ex.getFrench(), canEdit, context, entry, "example français",
                    "cdm-example-fra", i, exView, volume);
            exView.setTextColor(Color.parseColor(colorFrench));
            ((LinearLayout) v).addView(exView);
            i++;
        }
    }

    public static void addVedette(TextView v, ListEntry entry) {
        String romaji = entry.getRomajiDisplay();
        if (TextUtils.isEmpty(romaji)) {
            romaji = entry.getRomajiSearch();
        }
        String kanji = entry.getKanji();
        if (entry.isVerified()) {
            kanji = "<font color=" + colorJapanese + ">" + kanji + "</font>";
        } else {
            kanji = "<font color=" + colorPbJapanese + ">" + kanji + "</font>";
        }
        String vText = kanji +
                "   <font color=" + colorJapanese + ">【" + entry.getHiragana() + "】</font>   " +
                "   <font color=" + colorRomaji + ">(" + romaji + ")</font>";
        v.setText(Html.fromHtml(vText));
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
}
