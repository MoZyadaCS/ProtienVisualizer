package com.example.protienvisualizer.view;
import com.example.protienvisualizer.controller.Controller;
import com.example.protienvisualizer.model.Atom;
import com.example.protienvisualizer.model.IMolecule;
import com.example.protienvisualizer.model.Molecule;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The window presenter
 * This is where we communicate between the view and the model
 */
public class WindowPresenter {
	private double anchorX, anchorY;
	private double anchorAngleX = 0;
	private double anchorAngleY = 0;
	private final DoubleProperty angleX = new SimpleDoubleProperty(0);
	private final DoubleProperty angleY = new SimpleDoubleProperty(0);

	public Map<Sphere, Atom> sphereToAtomMap = new HashMap<>();
	public Map<Atom, Sphere> atomToSphereMap = new HashMap<>();
		private final Molecule molecule;
		private final Controller controller;
	public WindowPresenter (Controller controller, Molecule model) {
		this.molecule = model;
		this.controller = controller;
		if(true){
			addMolecule(controller,model);
		}
		if(false){
			addBonds(controller,model);
		}


}

	// the action listeners in the project
	public void ActionsHandler(){
		controller.getCenterPane().setOnScroll(e-> {
			if (e.getDeltaY() > 0) {
				controller.getZoomInButton().fire();
			} else {
				controller.getZoomOutButton().fire();
			}
		});

		controller.getCenterPane().setOnMouseClicked(e-> {
			if(e.getPickResult().getIntersectedNode() instanceof Sphere) {
				Atom atom = this.sphereToAtomMap.get(e.getPickResult().getIntersectedNode());
				controller.getSeqArea().getSelectionModel().select(atom.getAminoAcidIndex());
				controller.getSeqArea().getSelectionModel().select(atom.getAminoAcidIndex()+1);
				controller.getSeqArea().getSelectionModel().select(atom.getAminoAcidIndex()+2);
				((PhongMaterial)((Sphere) e.getPickResult().getIntersectedNode()).getMaterial()).setDiffuseColor(atom.getColor());
				int index = atom.getAminoAcidIndex();
				controller.getSeqArea().scrollTo(index);
				for(Atom atom1 : controller.pdbFile.getAtoms()){
					if(!(((PhongMaterial)this.atomToSphereMap.get(atom1).getMaterial()).getDiffuseColor() == atom1.getColor())){
						if(atom1.getAminoAcidIndex() == index){
							if(this.controller.getChoiceBox().getValue().equals("Atom Coloring")){
								this.atomToSphereMap.get(atom1).setMaterial(new PhongMaterial(atom1.getColor()));
							}
							else{
								this.atomToSphereMap.get(atom1).setMaterial(new PhongMaterial(controller.getAminoColor(atom1.getAminoAcid())));
							}
						}
					}
				}

			}
		});
		controller.getSeqArea().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> controller.onSeqAreaClicked());

	}





	// drawing the model methods
	private void addMolecule(Controller controller, IMolecule model){
		SmartGroup root = new SmartGroup();
		for (int i = 0; i < model.getNumberOfAtoms(); i++) {
				Sphere sphere = new Sphere(model.getAtom(i).getRadiusPM());
				PhongMaterial material = new PhongMaterial();
				material.setDiffuseColor(model.getAtom(i).getColor());
				material.setSpecularColor(Color.WHITE);
				sphere.setMaterial(material);
				sphere.setTranslateX(model.getLocation(i).getX());
			sphere.setTranslateY(model.getLocation(i).getY());
			sphere.setTranslateZ(model.getLocation(i).getZ());
			this.sphereToAtomMap.put(sphere, (Atom) model.getAtom(i));
			this.atomToSphereMap.put((Atom) model.getAtom(i), sphere);
			root.getChildren().add(sphere);
		}
		this.controller.getCenterPane().getChildren().add(root);
		initMouseControl(root, this.controller.getCenterPane().getScene());
	}
	private void addBonds(Controller controller, IMolecule model){
		SmartGroup root = new SmartGroup();
		for(Pair pair : model.bonds()) {
			Point3D a = model.getLocation((int)pair.getKey());
			Point3D b = model.getLocation((int)pair.getValue());
			var YAXIS = new Point3D(0, 100, 0);
			var midpoint = a.midpoint(b);
			var direction = b.subtract(a);
			var perpendicularAxis = YAXIS.crossProduct(direction);
			var angle = YAXIS.angle(direction);
			var cylinder = new Cylinder();
			cylinder.setRadius(0.5);
			cylinder.setRotationAxis(perpendicularAxis);
			cylinder.setRotate(angle);
			cylinder.setTranslateX(midpoint.getX());
			cylinder.setTranslateY(midpoint.getY());
			cylinder.setTranslateZ(midpoint.getZ());
			cylinder.setScaleY(a.distance(b) / cylinder.getHeight());
			cylinder.setMaterial(new PhongMaterial(Color.BLACK));
			root.getChildren().add(cylinder);
		}
		this.controller.getCenterPane().getChildren().add(root);
		initMouseControl(root, this.controller.getCenterPane().getScene());

		}

	// handling the rotation property
	private void initMouseControl(SmartGroup group, Scene scene) {
		Rotate xRotate;
		Rotate yRotate;
		group.getTransforms().addAll(
				xRotate = new Rotate(0, Rotate.X_AXIS),
				yRotate = new Rotate(0, Rotate.Y_AXIS)
		);
		xRotate.angleProperty().bind(angleX);
		yRotate.angleProperty().bind(angleY);

		scene.setOnMousePressed(event -> {
			anchorX = event.getSceneX();
			anchorY = event.getSceneY();
			anchorAngleX = angleX.get();
			anchorAngleY = angleY.get();
		});

		scene.setOnMouseDragged(event -> {
			angleX.set(0.5 * (anchorAngleX - (anchorY - event.getSceneY())));
			angleY.set(0.5 * (anchorAngleY + anchorX - event.getSceneX()));
		});
		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				// get the mid point of the screen
				Point3D midPoint = new Point3D(controller.getCenterPane().getWidth() / 2, controller.getCenterPane().getHeight() / 2, 0);
				// get the average distance from the mid point to an atom
				double averageDistance = 0;
				for(Atom atom : controller.pdbFile.getAtoms()){
					averageDistance += atom.getLocation().distance(midPoint);
				}
				averageDistance /= controller.pdbFile.getAtoms().size();
				angleY.set(angleY.get() + (averageDistance - controller.getCenterPane().getHeight() / 2) / 100);

			}
		};
		AtomicBoolean isRotating = new AtomicBoolean(false);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.SHIFT) {

				if(!isRotating.get()){
					timer.start();
					isRotating.set(true);
				}
				else {
					timer.stop();
					isRotating.set(false);
				}

			}
		});
	}
	// creating a new Group class to control the rotation
	public static class SmartGroup extends Group {

		Rotate r;
		Transform t = new Rotate();

		void rotateByX(int ang) {
			r = new Rotate(ang, Rotate.X_AXIS);
			t = t.createConcatenation(r);
			this.getTransforms().clear();
			this.getTransforms().addAll(t);
		}

		void rotateByY(int ang) {
			r = new Rotate(ang, Rotate.Y_AXIS);
			t = t.createConcatenation(r);
			this.getTransforms().clear();
			this.getTransforms().addAll(t);
		}
	}
	}


