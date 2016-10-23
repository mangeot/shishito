package jibiki.fr.shishito.Models;

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
