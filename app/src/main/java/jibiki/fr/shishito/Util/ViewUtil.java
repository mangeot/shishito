package jibiki.fr.shishito.Util;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jibiki.fr.shishito.Models.Example;
import jibiki.fr.shishito.Models.GramBlock;
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
            gram.setText("[" + gb.getGram() + "]");
            ((LinearLayout) v).addView(gram);
            for (int j = 0; j < gb.getSens().size(); j++) {
                TextView senseView = new TextView(context);
                senseView.setId(50*(i+1)+j);
                senseView.setLayoutParams(params);
                senseView.setText((j+1) + ". " + gb.getSens().get(j));
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
            String text = "\u25CF " + " [<font color=#cc0000>" + ex.getHiragana() + "</font>] [<font color=#0066ff>" + ex.getRomaji() + "</font>] " + ex.getFrench();
            exView.setText(Html.fromHtml(text));
            ((LinearLayout) v).addView(exView);
        }
    }
}
