package com.example.protienvisualizer.filemanagment;

import com.example.protienvisualizer.model.Atom;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class DataReader {
    private String directoryPath;
    List<File> files;
    List<PdbFile> pdbFiles;

    // constructor
    public DataReader(String directoryPath) {
        this.directoryPath = directoryPath;
        try {
            this.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<PdbFile> getPdbFiles() {
        return pdbFiles;
    }
    // do the process needed when initializing new reader
    public void process() throws IOException {
        this.readFiles();
        List<PdbFile> pdbFiles = new ArrayList<>();
        for (File file : files) {
            PdbFile pdbFile = new PdbFile();
            pdbFile.setFileName(file.getName());
            pdbFiles.add(pdbFile);
        }
        this.pdbFiles = pdbFiles;
    }
    // read the files in the current directory
    private void readFiles(){
        File directory = new File(this.directoryPath);
        List<File> files = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".pdb") || file.getName().endsWith(".pdb.gz")) {
                files.add(file);
            }
        }
        this.files = files;
    }
    // read the file content
    public static List<String> readFileContent(PdbFile pdbFile,String path) throws IOException {
        List<String> lines = new ArrayList<>();
        if(pdbFile.getFileName().endsWith(".pdb")){
            FileReader fileReader = new FileReader((path + "\\" + pdbFile.getFileName()));
            Scanner reader = new Scanner(fileReader);
            while(reader.hasNext()) {
                lines.add(reader.nextLine());
            }
            try {
                fileReader.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            InputStream fileStream = new FileInputStream((path + "\\" + pdbFile.getFileName()));
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream);
            Scanner reader = new Scanner(decoder);
            while(reader.hasNext()) {
                lines.add(reader.nextLine());
            }
            try {
                fileStream.close();
                gzipStream.close();
                decoder.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }
    // read atoms from the file content
    public static List<Atom> readAtoms(PdbFile file){
        List<Atom> atoms = new ArrayList<>();
        for(String line : file.getLines()){
            if(line.startsWith("ATOM ")){
                Atom atom = new Atom();
                String [] lineSplit = line.split(" ");
                String [] lineSplit2 = new String[12];
                int count = 0;
                for (String s : lineSplit) {
                    if (s.equals("") || s.equals(" ")) {
                        continue;
                    } else {
                        lineSplit2[count++] = s;
                    }
                }
                int id = Integer.parseInt(lineSplit2[1]);
                String letter = lineSplit2[2];
                String aminoAcid = lineSplit2[3];
                String molecule = lineSplit2[4];
                Point3D location = new Point3D(Double.parseDouble(lineSplit2[6]),Double.parseDouble(lineSplit2[7]),Double.parseDouble(lineSplit2[8]));
                atom.setId(id);
                atom.setLetter(letter);
                atom.setAminoAcid(aminoAcid);
                if(file.getAminoAcids().size() == 0){
                    file.addAminoAcid(aminoAcid);
                }
                else{
                    if(!(file.getAminoAcids().get(file.getAminoAcids().size() - 1).equals(aminoAcid))){
                        file.getAminoAcids().add(aminoAcid);
                    }
                }
                atom.setAminoAcidIndex(file.getAminoAcids().size() / 3);
                atom.setMolecule(molecule);
                atom.setLocation(location);
                atom.setColor(getColor(letter));
                atoms.add(atom);

            }
        }
        file.setAtoms(atoms);
        return atoms;
    }
    // get the color of each atom
    public static Color getColor(String atom){
        return switch (atom) {
            case "C" -> Color.BLACK;
            case "N" -> Color.BLUE;
            case "O" -> Color.RED;
            case "F", "Cl" -> Color.GREEN;
            case "Br" -> Color.DARKRED;
            case "I" -> Color.DARKVIOLET;
            case "HE", "NE", "AR", "KR", "XE" -> Color.CYAN;
            case "P" -> Color.color(254, 165, 0);
            case "S" -> Color.YELLOW;
            case "B" -> Color.color(245, 245, 220);
            case "LI", "NA", "K", "RB", "CS", "FR" -> Color.color(155, 38, 182);
            case "BE", "MG", "CA", "SR", "BA", "RA" -> Color.DARKGREEN;
            case "TI", "H" -> Color.GRAY;
            case "FE" -> Color.DARKORANGE;
            default -> Color.PINK;
        };
    }

}
