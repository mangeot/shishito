package jibiki.fr.shishito.Util;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.GramBlock;
import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.R;

/**
 * Created by tibo on 26/07/16.
 */
public final class ViewUtil {

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

    public static void addExamplesToView(View v, ArrayList<Example> examples, Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        if (examples.size() > 0) {
            TextView exTitle = new TextView(context);
            TextViewCompat.setTextAppearance(exTitle, android.R.style.TextAppearance_Large);
            exTitle.setText(R.string.example);
            ((LinearLayout) v).addView(exTitle);
        }
        for (Example ex: examples) {
            TextView exView = new TextView(context);
            TextViewCompat.setTextAppearance(exView, android.R.style.TextAppearance_Medium);
            String text = context.getString(R.string.example_content, ex.getKanji(), ex.getHiragana(), ex.getRomaji(), ex.getFrench());
            exView.setText(Html.fromHtml(text));
            ((LinearLayout) v).addView(exView);
        }
    }

    public static void addVedette(TextView v, ListEntry entry) {
        String romaji = entry.getRomajiDisplay();
        if (TextUtils.isEmpty(romaji)) {
            romaji = entry.getRomajiSearch();
        }
        String kanji = entry.getKanji();
        if (kanji.contains("??")) {
            kanji = "<font color=#ffcc00>" + kanji + "</font>";
        }
        String vText = kanji +
                "   [<font color=#cc0000>" + entry.getHiragana() + "</font>]   " +
                "   [<font color=#cc0000>" + romaji + "</font>]";
        v.setText(Html.fromHtml(vText));
    }

    public static void addVerified(View v, ListEntry entry) {
        if (!entry.isVerified()) {
            TextView verif = new TextView(v.getContext());
            verif.setText(R.string.verif);
            ((LinearLayout) v).addView(verif);
        }
    }
}
