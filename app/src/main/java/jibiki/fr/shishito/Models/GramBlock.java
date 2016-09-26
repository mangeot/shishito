package jibiki.fr.shishito.Models;

import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tibo on 26/07/16.
 * A class representing a grammatical block from a dictionary definition.
 */
public class GramBlock implements Serializable{

    private String gram;
    private Node gramBlockNode;
    private ArrayList<String> sens;
    private ArrayList<String> enSens;

//    public GramBlock() {
//        sens = new ArrayList<>();
//    }

    public GramBlock(org.w3c.dom.Node newGramBlockNode) {
        gramBlockNode = newGramBlockNode;
        sens = new ArrayList<>();
    }

    public Node getNode() {
        return gramBlockNode;
    }

    public String getGram() {
        return gram;
    }

    public void setGram(String gram) {
        this.gram = gram;
    }

    public ArrayList<String> getSens() {
        return sens;
    }

    public void addSens(String sense) {
        this.sens.add(sense);
    }

    public void addEnSens(String enSense) {
        if (enSens == null) {
            enSens = new ArrayList<>();
        }
        enSens.add(enSense);
    }

}
