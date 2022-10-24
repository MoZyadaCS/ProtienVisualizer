package com.example.protienvisualizer.model;

import javafx.geometry.Point3D;
import javafx.util.Pair;

import java.util.List;

public interface IMolecule {
	String getName();

	String getFormula();

	int getNumberOfAtoms();

	IAtom getAtom(int pos);

	Point3D getLocation(int pos);

	List<Pair<Integer, Integer>> bonds();

	void setAtoms(List<Atom> atoms);
}
