package com.example.protienvisualizer.model;

import javafx.geometry.Point3D;
import javafx.util.Pair;

import java.util.List;

public class Molecule implements IMolecule {
    List<Atom> atoms;

    public Molecule(){

    }
    public List<Atom> getAtoms(){
        return atoms;
    }
    public void setAtoms(List<Atom> atoms){
        this.atoms = atoms;
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getFormula() {
        return null;
    }

    @Override
    public int getNumberOfAtoms() {
        return this.atoms.size();
    }

    @Override
    public IAtom getAtom(int pos) {
        return this.atoms.get(pos);
    }

    @Override
    public Point3D getLocation(int pos) {
        return this.atoms.get(pos).getLocation();
    }

    @Override
    public List<Pair<Integer, Integer>> bonds() {
        return null;
    }
}
