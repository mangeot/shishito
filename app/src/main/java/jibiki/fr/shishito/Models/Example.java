package jibiki.fr.shishito.Models;

import java.io.Serializable;

/**
 * Created by tibo on 06/01/16.
 * This class represents an example from the dictionary.
 */
public class Example implements Serializable{
    private String kanji;
    private String hiragana;
    private String romaji;

    private String french;

    public Example(){}

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

    public String getRomaji() {
        return romaji;
    }

    public void setRomaji(String romaji) {
        this.romaji = romaji;
    }

    public String getFrench() {
        return french;
    }

    public void setFrench(String french) {
        this.french = french;
    }
}
