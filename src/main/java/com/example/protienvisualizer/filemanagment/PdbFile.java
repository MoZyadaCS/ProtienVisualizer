package com.example.protienvisualizer.filemanagment;

import com.example.protienvisualizer.model.Atom;
import java.util.ArrayList;
import java.util.List;

public class PdbFile {
    private String fileName;
    private List<String> lines;
    List<Atom> atoms = new ArrayList<>();
    List<String> aminoAcids = new ArrayList<>();
    private String ProteinName;
    // constructor
    public PdbFile(){
        this.lines = new ArrayList<>();
    }
    // getters and setters
    public void setFileName(String name){
        this.fileName = name;
    }
    public void setLines(List<String> lines){
        this.lines = lines;
    }
    public String getFileName(){
        return this.fileName;
    }
    public List<String> getLines(){
        return this.lines;
    }
    public List<Atom> getAtoms(){
        return this.atoms;
    }

    public void setAtoms(List<Atom> atoms) {
        this.atoms = atoms;
    }
    public void addAminoAcid(String amino){
        this.aminoAcids.add(amino);
    }
    public List<String> getAminoAcids(){
        return this.aminoAcids;
    }
    public void setProteinName(String name){
        this.ProteinName = name;
    }
    public String getProteinName(){
        return this.ProteinName;
    }
}
