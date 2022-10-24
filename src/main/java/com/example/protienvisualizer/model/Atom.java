package com.example.protienvisualizer.model;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class Atom implements IAtom {
    private int id;
    private String letter;
    private int radiusPM;
    private Color color;
    private Point3D location;
    private String aminoAcid;
    private String molecule;
    private int AminoAcidIndex;
    public Atom(){this.radiusPM =7;}

    @Override
    public String getName() {
        return "Atom";
    }
    public String getLetter() {
        return letter;
    }
    public int getRadiusPM() {
        return 7;
    }
    public Color getColor() {
        return color;
    }
    public String getAminoAcid(){
        return aminoAcid;
    }
    public void setMolecule(String molecule){
        this.molecule = molecule;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setLetter(String letter){
        this.letter = letter;
    }
    public void setColor(Color color){
        this.color = color;
    }
    public void setLocation(Point3D location){
        this.location = location;
    }
    public void setAminoAcid(String aminoAcid){
        this.aminoAcid = aminoAcid;
    }
    public Point3D getLocation(){
        return location;
    }
    public double getX(){
        return location.getX();
    }
    public double getY(){
        return location.getY();
    }
    public double getZ(){
        return location.getZ();
    }
    public void setX(double x){
        location = new Point3D(x, location.getY(), location.getZ());
    }
    public void setY(double y){
        location = new Point3D(location.getX(), y, location.getZ());
    }
    public void setZ(double z){
        location = new Point3D(location.getX(), location.getY(), z);
    }
    public void setAminoAcidIndex(int index){
        this.AminoAcidIndex = index;
    }
    public int getAminoAcidIndex(){
        return AminoAcidIndex;
    }

}
