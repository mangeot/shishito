/* Copyright (C) 2016 Thibaut Le Guilly et Mathieu Mangeot
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package jibiki.fr.shishito.Models;

import org.w3c.dom.Node;

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
