package jibiki.fr.shishito.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tibo on 26/07/16.
 */
public class GramBlock implements Serializable{

    String gram;
    ArrayList<String> sens;
    ArrayList<String> enSens;

    public GramBlock() {
        sens = new ArrayList<>();
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
