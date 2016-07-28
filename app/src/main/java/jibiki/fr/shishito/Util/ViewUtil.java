package jibiki.fr.shishito.Util;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.GramBlock;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.R;
import jibiki.fr.shishito.SearchActivity;

/**
 * Created by tibo on 26/07/16.
 */
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


    public static void addGramBlocksToView(View v, ArrayList<GramBlock> gramBlocks, Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 5, 0, 5);
        for (int i = 0; i < gramBlocks.size(); i++) {
            GramBlock gb = gramBlocks.get(i);
            TextView gram = new TextView(context);
            gram.setId(i);
            TextViewCompat.setTextAppearance(gram, android.R.style.TextAppearance_Small);
            gram.setLayoutParams(params);
            if (gb.getGram() != null) {
                gram.setText(context.getString(R.string.in_bracket, gb.getGram()));
            }
            ((LinearLayout) v).addView(gram);
            for (int j = 0; j < gb.getSens().size(); j++) {
                TextView senseView = new TextView(context);
                senseView.setId(50*(i+1)+j);
                senseView.setLayoutParams(params);
                senseView.setText(Html.fromHtml((j+1) + ". " + gb.getSens().get(j)));
                TextViewCompat.setTextAppearance(senseView, android.R.style.TextAppearance_Medium);
                ((LinearLayout) v).addView(senseView);
            }
        }
    }

    private static SpannableString getClickSpannable(String input, boolean clickable, final SearchActivity context, ListEntry entry, final String title, final String xpath) {
        SpannableString ss = new SpannableString(Html.fromHtml(input));
        if (clickable) {
            final String contribId = entry.getContribId();
            final String word = removeFancy(input);
            ClickableSpan cs = new ClickableSpan() {
                String s = word;

                @Override
                public void onClick(View widget) {
                    context.putFastEdit(contribId, xpath, s, title);
                }
                @Override
                public void updateDrawState(TextPaint ds) {// override updateDrawState
                    ds.setUnderlineText(false); // set to false to remove underline
                }
            };
            ss.setSpan(cs, 0, word.length(), 0);
        }
        return ss;

    }

    public static void addExamplesToView(View v, ListEntry entry, SearchActivity context, boolean canEdit) {
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
        for (Example ex: examples) {
            TextView exView = new TextView(context);
            exView.setLineSpacing(5, 1);
            TextViewCompat.setTextAppearance(exView, android.R.style.TextAppearance_Medium);

            String xpath = XMLUtils.getTransformedXPath("cdm-example-jpn", i, context.getVolume());
            exView.append(getClickSpannable(ex.getKanji(), canEdit, context, entry, "example kanji", xpath));
            xpath = XMLUtils.getTransformedXPath("cesselin-example-romaji", i, context.getVolume());
            exView.append(getClickSpannable("<font color="+colorRomaji+">(" + ex.getRomaji()+")</font>", canEdit, context, entry, "example romaji", xpath));
            exView.append("  ");
            xpath = XMLUtils.getTransformedXPath("cdm-example-fra", i, context.getVolume());
            exView.append(getClickSpannable(ex.getFrench(), canEdit, context, entry, "example français", xpath));
            exView.setMovementMethod(LinkMovementMethod.getInstance());
//            String text = context.getString(R.string.example_content, ex.getKanji(), "<font color="+colorRomaji+">(" + ex.getRomaji()+")</font>", ex.getFrench());
//            exView.setText(Html.fromHtml(text));
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
            kanji = "<font color="+colorJapanese+">" + kanji + "</font>";
        }
        else {
            kanji = "<font color="+colorPbJapanese+">" + kanji + "</font>";
        }
        String vText = kanji +
                "   <font color="+colorJapanese+">【" + entry.getHiragana() + "】</font>   " +
                "   <font color="+colorRomaji+">(" + romaji + ")</font>";
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
