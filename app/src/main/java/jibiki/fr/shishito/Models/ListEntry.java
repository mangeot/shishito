package jibiki.fr.shishito.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tibo on 29/11/15.
 */
public class ListEntry implements Serializable {

    private String kanji;
    private String hiragana;
    private String romanji;
    private String gram;
    private String definition;

    private ArrayList<Example> examples;

    public ListEntry() {
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public String getHiragana() {
        return hiragana;
    }

    public void setHiragana(String hiragana) {
        this.hiragana = hiragana;
    }

    public String getRomanji() {
        return romanji;
    }

    public void setRomanji(String romanji) {
        this.romanji = romanji;
    }


    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }


    public String getGram() {
        return gram;
    }

    public void setGram(String gram) {
        this.gram = gram;
    }

    public ArrayList<Example> getExamples() {
        return examples;
    }

    public void setExamples(ArrayList<Example> examples) {
        this.examples = examples;
    }

}
