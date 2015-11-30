package jibiki.fr.shishito;

/**
 * Created by tibo on 29/11/15.
 */
public class ListEntry {

    private String kanji;
    private String hiragana;
    private String romanji;

    public ListEntry(){}

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
}
