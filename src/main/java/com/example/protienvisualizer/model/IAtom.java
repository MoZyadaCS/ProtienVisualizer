package com.example.protienvisualizer.model;

import javafx.scene.paint.Color;

public interface IAtom {
	String getName();

	String getLetter();

	int getRadiusPM();

	Color getColor();

	String getAminoAcid();

	void setColor(Color aminoColor);
}
