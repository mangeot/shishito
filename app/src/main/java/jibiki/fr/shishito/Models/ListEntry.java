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

    private String entryId;

    private String contribId;

    private ArrayList<Example> examples;

    private ArrayList<GramBlock> gramBlocks;

    public ListEntry() {
        gramBlocks = new ArrayList<>();
        examples = new ArrayList<>();
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

    public ArrayList<Example> getExamples() {
        return examples;
    }

    public void addExample(Example example) {
        this.examples.add(example);
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getContribId() {
        return contribId;
    }

    public void setContribId(String contribId) {
        this.contribId = contribId;
    }

    public ArrayList<GramBlock> getGramBlocks() {
        return gramBlocks;
    }

    public void addGramBlock(GramBlock gb) {
        gramBlocks.add(gb);
    }
}
