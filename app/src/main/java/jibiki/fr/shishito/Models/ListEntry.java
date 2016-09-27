package jibiki.fr.shishito.Models;

import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tibo on 29/11/15.
 * A class representing an entry from the dictionary.
 */
public class ListEntry implements Serializable {

    private Volume volume;
    private transient Node entryNode;
    private transient Node kanjiNode;
    private transient Node hiraganaNode;
    private String romajiDisplay;
    private String romajiSearch;

    private String entryId;

    private String contribId;

    private ArrayList<Example> examples;

    private ArrayList<GramBlock> gramBlocks;

    private boolean verified;

//    private void writeObject(ObjectOutputStream out) throws IOException{
//        out.defaultWriteObject();
//    }

    public ListEntry(Node theEntryNode, Volume theVolume) {
        gramBlocks = new ArrayList<>();
        examples = new ArrayList<>();
        volume = theVolume;
        entryNode = theEntryNode;
    }

    public Volume getVolume() {
        return volume;
    }

    public Node getNode() {
        return entryNode;
    }

    public Node getKanjiNode() {
        return kanjiNode;
    }

    public void setKanjiNode(Node kanji) {
        this.kanjiNode = kanji;
    }

    public Node getHiraganaNode() {
        return hiraganaNode;
    }

    public void setHiraganaNode(Node hiragana) {
        this.hiraganaNode = hiragana;
    }

    public String getRomajiDisplay() {
        return romajiDisplay;
    }

    public void setRomajiDisplay(String romajiDisplay) {
        this.romajiDisplay = romajiDisplay;
    }

    public ArrayList<Example> getExamples() {
        return examples;
    }

    public String getRomajiSearch() {
        return romajiSearch;
    }

    public void setRomajiSearch(String romajiSearch) {
        this.romajiSearch = romajiSearch;
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
