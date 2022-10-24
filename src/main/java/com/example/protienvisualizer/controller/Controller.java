package com.example.protienvisualizer.controller;
// importing the necessary libraries
import com.example.protienvisualizer.filemanagment.DataReader;
import com.example.protienvisualizer.filemanagment.PdbFile;
import com.example.protienvisualizer.model.Atom;
import com.example.protienvisualizer.model.Molecule;
import com.example.protienvisualizer.view.WindowPresenter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
// the controller class
public class Controller {
    public PdbFile pdbFile;
    private String directoryPath;
    public List<PdbFile> pdbFiles = new ArrayList<>();
    final static String apiUrl = "https://data.rcsb.org/rest/v1/holdings/current/entry_ids";
    public WindowPresenter presenter;
    @FXML
    private ListView<String> filesList;
    @FXML
    private Molecule molecule;
    @FXML
    private Pane centerPane;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private CheckBox ballsCheckButton;
    @FXML
    private Slider scaleSlider;
    @FXML
    private CheckBox sticksCheckButton;
    @FXML
    private Button zoomIn;
    @FXML
    private Button zoomOut;
    @FXML
    private ListView<Character> seqArea;
    @FXML
    private CheckBox darkMode;


    // initializer method
    public void initialize() {
        this.getChoiceBox().getItems().add("Atom Coloring");
        this.getChoiceBox().getItems().add("Amino Acid Coloring");
        this.getChoiceBox().setValue("Atom Coloring");
        this.getChoiceBox().getItems().forEach(item -> this.getChoiceBox().setOnAction(e -> viewNewColor()));
        this.getFilesList().addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{
            if(e.getClickCount() == 2){
                // get the file name
                String fileName = this.getFilesList().getSelectionModel().getSelectedItem();
                try {
                    this.readProtien();
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        darkMode.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            File css = new File("src/main/resources/com/example/protienvisualizer/styles.css");
            if (isSelected) {
                this.getCenterPane().getScene().getStylesheets().add(css.toURI().toString());
            } else {
                this.getCenterPane().getScene().getStylesheets().remove(css.toURI().toString());
            }
        });


    }
    // method to handle adding a new Directory
    public void addMenuItemClicked(){
        // create a directory chooser
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a directory");
        directoryChooser.setInitialDirectory(new File("."));
        // get the selected directory
        File selectedDirectory = directoryChooser.showDialog(null);
        String path = selectedDirectory.getAbsolutePath();
        // set the directory path to the folder path
        this.directoryPath = path;
        // read the files in the directory
        DataReader dataReader = new DataReader(path);
        List<PdbFile> pdbFiles = dataReader.getPdbFiles();
        this.pdbFiles = pdbFiles;
        // add the files to the list view
        this.filesList.getItems().clear();
        this.filesList.setFixedCellSize(20);
        for (PdbFile pdbFile : pdbFiles) {
            this.filesList.getItems().add(pdbFile.getFileName());
        }
    }
    // method to handle closing the application
    public void closeMenuItemClicked() {
        System.exit(0);
    }
    // method to view the file content as a text
    public void onTextButtonClicked() {
        // get the selected file name
        String fileName = this.filesList.getSelectionModel().getSelectedItem();
        List<String> lines = new ArrayList<>();
        // get the file from the list of files
        for (PdbFile pdbFile : pdbFiles) {
            // checking if the filename is the same as the selected file name
            if (pdbFile.getFileName().equals(fileName)) {
                lines = pdbFile.getLines();
                if(pdbFile.getLines().size() == 0){
                    try {
                        lines = DataReader.readFileContent(pdbFile,directoryPath);
                        pdbFile.setLines(lines);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // create a new window
        Stage stage = new Stage();
        TextArea textArea = new TextArea();
        for(String line : lines){
            textArea.appendText(line + "\n");
        }
        // set the scene of the window to the text area
        Scene scene = new Scene(textArea, 500, 500);
        stage.setScene(scene);
        stage.show();
    }
    // method to clear all the data from the screen
    public void onClearMenuItemClicked() {
        // setting all the data to null
        this.pdbFile = null;
        this.pdbFiles = new ArrayList<>();
        this.getCenterPane().getChildren().clear();
        this.presenter = null;
        this.seqArea.getItems().clear();
        this.filesList.getItems().clear();
        this.pdbFiles = new ArrayList<>();
    }
    // method to view an about menu to tell some information about the application
    public void onAboutMenuItemClicked(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About");
        alert.setContentText("This is a program to read pdb files content, get some statistics about them and visualize them");
        alert.showAndWait();
    }
    // method to handle the event of clicking the statistics button
    public void onStatisticsButtonClicked() {
        // getting the selected file from the files list
        String fileName = this.filesList.getSelectionModel().getSelectedItem();for (PdbFile pdbFile : pdbFiles) {
            // searching for the file with the same name
            if (pdbFile.getFileName().equals(fileName)) {
                // creating a new window
                Stage stage = new Stage();
                // reading the file content if not read yet
                if(pdbFile.getLines().size() == 0){
                    try {
                        List<String> lines = DataReader.readFileContent(pdbFile,directoryPath);
                        pdbFile.setLines(lines);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // get the statistics of the file
                List<String> stats = this.getStatisticsFromFile(pdbFile);
                // create a Bar chart with the data
                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                barChart.setTitle("Amino Acid Statistics");
                xAxis.setLabel("Amino Acids");
                yAxis.setLabel("Number of Occurrences");
                XYChart.Series series = new XYChart.Series();
                series.setName("Amino Acids");
                // adding the data to the series
                for (String stat : stats) {
                    String[] split = stat.split(":");
                    if(split.length == 3){
                        series.getData().add(new XYChart.Data(split[0], Integer.parseInt(split[1])));
                    }
                }
                // adding the series to the chart
                barChart.getData().add(series);
                String stat1 = "";
                String stat2 = "";
                for(String line : stats){
                    String[] split = line.split(":");

                    if(split.length != 3){
                        if(stat1.equals("")){
                            stat1 = line;
                        }
                        else {
                            stat2 = line;
                        }
                    }
                }
                // setting the font and text of the new window
                Font font = new Font("SanSerif", 16);
                Label l1 = new Label(stat1);
                Label l2 = new Label(stat2);
                l1.setFont(font);
                l2.setFont(font);
                // creating a VBox to hold the data and the chart
                VBox vBox = new VBox();
                vBox.getChildren().addAll(l1,l2,barChart);
                // setting the scene of the window to the VBox
                Scene scene = new Scene(vBox, 700, 500);
                stage.setScene(scene);
                // showing the window
                stage.show();

            }
        }
    }
    // method to view the protein on the screen
    public void readProtien() throws URISyntaxException {
        // clearing the pane from any previous data
        this.getCenterPane().getChildren().clear();
        // getting the selected file from the files list
        String fileName = this.filesList.getSelectionModel().getSelectedItem();
        // a boolean value to differ between the files read from the directory and the files read from the API
        boolean read = false;
        // searching for the file with the same name
        for (PdbFile pdbFile : pdbFiles) {
            if (pdbFile.getFileName().equals(fileName)) {
                this.pdbFile = pdbFile;
                // set read to true so we don't get any data from the api
                read = true;
                // read the file content if not read yet
                if (pdbFile.getLines().size() == 0) {
                    try {
                        List<String> lines = DataReader.readFileContent(pdbFile, directoryPath);
                        pdbFile.setLines(lines);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // get the protein name from the file content
                String protienName = pdbFile.getLines().get(1).substring(pdbFile.getLines().get(1).indexOf("FOR") + 4);
                if (pdbFile.getLines().get(2).startsWith("TITLE")) {
                    protienName += pdbFile.getLines().get(2).substring(pdbFile.getLines().get(2).indexOf("TITLE") + 10);
                }
                pdbFile.setProteinName(protienName);
                List<Atom> atoms = DataReader.readAtoms(pdbFile);
                pdbFile.setAtoms(atoms);
                // calling the method to view the file
                viewFile(pdbFile);
            }
        }
        // read the file from the api if not found in the pdb file
        if (!read) {
            PdbFile file = new PdbFile();
            file.setFileName(fileName);
            // create a new HttpRequest to the api
            HttpRequest postReq = HttpRequest.newBuilder().uri(new URI("https://files.rcsb.org/download/"+fileName+".pdb")).build();
            // create a client to send the request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = null;
            // send the request
            try {
                postResponse =  client.send(postReq, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // get the response body as a string
            String data = postResponse.body();
            file.setLines(Arrays.asList(data.split("\n")));
            this.pdbFiles.add(file);
            List<Atom> atoms = DataReader.readAtoms(file);
            // set the atoms of the file
            file.setAtoms(atoms);
            // get the protein name from the file content
            String protienName = file.getLines().get(1).substring(file.getLines().get(1).indexOf("TITLE") + 10);
            if (file.getLines().get(2).startsWith("TITLE")) {
                protienName += file.getLines().get(2).substring(file.getLines().get(2).indexOf("TITLE") + 10);
            }
            file.setProteinName(protienName);
            this.pdbFile = file;
            this.pdbFiles.add(file);
            // call the method to view the file
            viewFile(file);
        }
        // adding the actions handlers from the presenter
        presenter.ActionsHandler();
    }
    // method to get the data from the api
    public void onApiButtonClicked() throws URISyntaxException {
        // create the http request
        HttpRequest postReq = HttpRequest.newBuilder().uri(new URI(apiUrl)).build();
        // create a client to send the request
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = null;
        // sending the request
        try {
            postResponse =  client.send(postReq, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // getting the response body as string
        String response = postResponse.body();
        // filtering the response to make it clear and remove unnecessary data
        List<String> lines = Arrays.asList(response.split("\",\""));
        lines.set(0,lines.get(0).substring(2));
        lines.set(lines.size()-1,lines.get(lines.size()-1).substring(0,lines.get(lines.size()-1).length()-2));
        // clearing the files list from any previous data
        this.filesList.getItems().clear();
        this.filesList.setFixedCellSize(20);
        // adding the files id to the files list
        for(String line : lines){
            this.filesList.getItems().add(line);
        }
    }
    // method to handle the zoom in operation
    public void onZoomIn( ){
        zoomIn.setOnAction(event -> this.getCenterPane().getChildren().forEach(node -> node.setTranslateZ(node.getTranslateZ() - 50)));
    }

    // method to handle the zoom out operation
    public void onZoomOut( ){
        zoomOut.setOnAction(event -> this.getCenterPane().getChildren().forEach(node -> {
            node.setTranslateZ(node.getTranslateZ() + 50);
        }));
    }
    // method to handle chaning the scale of the atoms and bonds
    public void ChangeScale( ){
        Pane pane = this.getCenterPane();
        this.getScaleSlider().setMin(0);
        this.getScaleSlider().setMax(2.5);
        this.getScaleSlider().valueProperty().addListener((observable, oldValue, newValue) -> {
            pane.getChildren().forEach(node -> {
                if (node instanceof WindowPresenter.SmartGroup) {
                    for (Node child : ((WindowPresenter.SmartGroup) node).getChildren()) {
                        child.setScaleX(newValue.doubleValue());
                        child.setScaleY(newValue.doubleValue());

                    }
                }
            });
        });
    }
    // method to handle viewing the atoms or not
    public void onBallsChecked( ){
        if(this.getBallsCheckButton().isSelected()){
            getCenterPane().getChildren().forEach(node -> {
                if(node instanceof WindowPresenter.SmartGroup){
                    for (Node child : ((WindowPresenter.SmartGroup) node).getChildren()) {
                        if(child instanceof Sphere){
                            child.setVisible(true);
                        }
                    }
                }
            });
        }
        else{
            getCenterPane().getChildren().forEach(node -> {
                if(node instanceof WindowPresenter.SmartGroup){
                    for (Node child : ((WindowPresenter.SmartGroup) node).getChildren()) {
                        if(child instanceof Sphere){
                            child.setVisible(false);
                        }
                    }
                }
            });
        }
    }
    // method to handle viewing the bonds or not
    public void onSticksChecked( ){
        if(this.getSticksCheckButton().isSelected()){
            getCenterPane().getChildren().forEach(node -> {
                if(node instanceof Group){
                    Group n = (Group) node;
                    n.getChildren().forEach(node1 -> {
                        if(node1 instanceof Cylinder){
                            node1.setVisible(true);
                        }
                    });
                }
            });
        }
        else{
            this.getCenterPane().getChildren().forEach(node -> {
                if(node instanceof Group){
                    Group n = (Group) node;
                    n.getChildren().forEach(node1 -> {
                        if(node1 instanceof Cylinder){
                            node1.setVisible(false);
                        }
                    });
                }
            });
        }
    }
    // method to handle selecting some amino acids to be shown
    public void onSeqAreaClicked(){
        // get the selected indexes
        List<Integer> selected = this.seqArea.getSelectionModel().getSelectedIndices();
        List<Integer> ourList = new ArrayList<>();
        // change every index to the aminoAcid index
        for(int i=0;i<selected.size();i++){
            ourList.add(selected.get(i) / 3);
        }
        // looping on every atom to check if its amino acid is selected
        for(Atom atom : this.pdbFile.getAtoms()){
            // get the corresponding sphere for the current atom
            Sphere sphere = this.presenter.atomToSphereMap.get(atom);
            // if not in the selected list change the color to while
            if(!ourList.contains(atom.getAminoAcidIndex())){
                ((PhongMaterial)sphere.getMaterial()).setDiffuseColor(Color.WHITE);
            }
            // if selected keep its color the same
            else{
                if(this.getChoiceBox().getValue().equals("Atom Coloring")){
                    ((PhongMaterial)sphere.getMaterial()).setDiffuseColor(atom.getColor());
                }
                else{
                    ((PhongMaterial)sphere.getMaterial()).setDiffuseColor(this.getAminoColor(atom.getAminoAcid()));
                }
            }
        }
    }
    // clear all the selections
    public void onClearButtonClicked(){
        // clear the selections from the sequence
        this.seqArea.getSelectionModel().clearSelection();
        // give each sphere it's original color
        for (Atom atom : this.pdbFile.getAtoms()) {
            Sphere sphere = this.presenter.atomToSphereMap.get(atom);
            if(this.getChoiceBox().getValue().equals("Atom Coloring")){
                ((PhongMaterial)sphere.getMaterial()).setDiffuseColor(atom.getColor());
            }
            else{
                ((PhongMaterial)sphere.getMaterial()).setDiffuseColor(this.getAminoColor(atom.getAminoAcid()));
            }
        }

    }
    public void ViewProtienName(){
        if(this.pdbFile == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No PDB File Selected");
            alert.setContentText("Please select a PDB file first");
            alert.showAndWait();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Protein Name");
            alert.setHeaderText("Protein Name");
            alert.setContentText(this.pdbFile.getProteinName());
            alert.showAndWait();
        }

    }
    public void sendEmail(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Details");
        alert.setHeaderText("Have new ideas or comments?");
        alert.setContentText("Please contact us at: \n" +
                "Email: norah.alsofi@student.uni-tuebingen.de \n" +
                "Phone: + 49 123-456-7890 \n");
        alert.showAndWait();
    }

    // helper methods
    // method to change the color when changing to aminoAcid coloring
    public void viewNewColor() {
        String choice = this.getChoiceBox().getValue();
        if (choice.equals("Atom Coloring")) {
            try {
                this.readProtien();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else if (choice.equals("Amino Acid Coloring")) {
            for (Atom atom : molecule.getAtoms()){
                presenter.atomToSphereMap.get(atom).setMaterial(new PhongMaterial(getAminoColor(atom.getAminoAcid())));
            }
        }
    }
    // method to get the color for a given amino acid
    public Color getAminoColor(String aminoAcid){
        return switch (aminoAcid) {
            case "ASP", "GLU" -> Color.web("rgb(230,10,10)");
            case "ARG", "LYS" -> Color.BLUE;
            case "CYS", "MET" -> Color.YELLOW;
            case "PHE", "TYR" -> Color.web("rgb(50,50,170)");
            case "GLY" -> Color.web("rgb(235,235,235)");
            case "HIS" -> Color.web("rgb(130,130,210)");
            case "ALA" -> Color.web("rgb(200,200,200)");
            case "SER", "THR" -> Color.web("rgb(250,150,0)");
            case "ASN", "GLN" -> Color.CYAN;
            case "LEU", "VAL", "ILE" -> Color.GREEN;
            case "TRP" -> Color.PINK;
            default -> Color.web("rgb(220,150,130)");
        };
    }

    private List<String> getStatisticsFromFile(PdbFile file){
        Map<String,Integer> aminoAcids = new HashMap<>();
        for(String line : file.getLines()){
            if(line.startsWith("SEQRES")){
                String[] lineSplit = line.split(" ");
                for(int i = 4; i < lineSplit.length; i++){
                    if(lineSplit[i].length() != 3){
                        continue;
                    }
                    if(aminoAcids.containsKey(lineSplit[i])){
                        aminoAcids.put(lineSplit[i],aminoAcids.get(lineSplit[i])+1);
                    }
                    else{
                        aminoAcids.put(lineSplit[i],1);
                    }
                }
            }
            else if(line.startsWith("ATOM")) break;
        }
        int total = 0;
        for(Integer value : aminoAcids.values()){
            total += value;
        }
        List<String> statistics = new ArrayList<>();
        statistics.add("Number of amino acids: " + aminoAcids.size());
        for(String key : aminoAcids.keySet()){
            statistics.add(key + ":" + aminoAcids.get(key) + ":" + String.format("%.2f",(double)aminoAcids.get(key)/total*100) + "%");
        }

        int countAtoms = 0;
        for(String line : file.getLines()){
            if(line.startsWith("ATOM")){
                countAtoms++;
            }
        }
        statistics.add("Number of atoms: " + countAtoms);
        return statistics;
    }
    private String getSequence(PdbFile file){
        StringBuilder sequence = new StringBuilder();
        for(String line : file.getLines()){
            if(line.startsWith("SEQRES")){
                String[] lineSplit = line.split(" ");
                for(int i = 4; i < lineSplit.length; i++){
                    if(lineSplit[i].length() != 3){
                        continue;
                    }
                    if(Character.isLetter(lineSplit[i].charAt(0))){
                        sequence.append(lineSplit[i]);
                    }
                }
            }
            else if(line.startsWith("ATOM")) break;
        }
        return sequence.toString();
    }
    public void viewFile(PdbFile pdbFile){
        this.getChoiceBox().setValue("Atom Coloring");
        String sequence = this.getSequence(pdbFile);
        ObservableList<Character> list = FXCollections.observableArrayList();
        for(char c : sequence.toCharArray()){
            list.add(c);
        }
        this.seqArea.getItems().clear();
        this.seqArea.setItems(list);
        this.seqArea.setEditable(false);
        this.seqArea.setOrientation(Orientation.HORIZONTAL);
        this.seqArea.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        List<Atom> atoms = DataReader.readAtoms(pdbFile);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        for(Atom atom : atoms){
            if (atom.getX() < minX) minX = atom.getX();
            if (atom.getY() < minY) minY = atom.getY();
            if (atom.getZ() < minZ) minZ = atom.getZ();
        }
        // shift all the atoms to the positive quadrant
        for(Atom atom : atoms){
            atom.setX(atom.getX() - minX);
            atom.setY(atom.getY() - minY);
            atom.setZ(atom.getZ() - minZ);
        }
        // multiply all the atoms by 4 to make them bigger
        for(Atom atom : atoms){
            atom.setX(atom.getX() * 4);
            atom.setY(atom.getY() * 4);
            atom.setZ(atom.getZ() * 4);
        }
        // scale the atoms to fit the screen
        double maxX = 0;
        double maxY = 0;
        double maxZ = 0;
        for(Atom atom : atoms){
            if (atom.getX() > maxX) maxX = atom.getX();
            if (atom.getY() > maxY) maxY = atom.getY();
            if (atom.getZ() > maxZ) maxZ = atom.getZ();
        }
        double scaleX = 300 / maxX;
        double scaleY = 300 / maxY;
        double scaleZ = 300 / maxZ;
        double scale = Math.min(scaleX, Math.min(scaleY, scaleZ));
        for(Atom atom : atoms){
            atom.setX(atom.getX() * scale);
            atom.setY(atom.getY() * scale);
            atom.setZ(atom.getZ() * scale);
        }
        // move the atoms to the center of the screen
        double centerX = 150;
        double centerY = 150;
        double centerZ = 150;
        for(Atom atom : atoms){
            atom.setX(atom.getX() + centerX);
            atom.setY(atom.getY() + centerY);
            atom.setZ(atom.getZ() + centerZ);
        }
        Molecule molecule = new Molecule();
        this.molecule = molecule;
        molecule.setAtoms(atoms);
        WindowPresenter presenter = new WindowPresenter(this,molecule);
        this.presenter = presenter;
        Camera camera = new PerspectiveCamera();
        getCenterPane().getScene().setCamera(camera);
    }



    // getters and setters
    public ChoiceBox<String> getChoiceBox(){
        return this.choiceBox;
    }
    public Button getZoomInButton(){
        return this.zoomIn;
    }
    public Button getZoomOutButton(){
        return this.zoomOut;
    }
    public ListView<Character> getSeqArea(){
        return this.seqArea;
    }
    public CheckBox getBallsCheckButton() {
        return ballsCheckButton;
    }

    public Pane getCenterPane() {
        return centerPane;
    }
    public Slider getScaleSlider() {
        return scaleSlider;
    }

    public CheckBox getSticksCheckButton() {
        return sticksCheckButton;
    }
    public ListView<String> getFilesList() {
        return filesList;
    }

}
