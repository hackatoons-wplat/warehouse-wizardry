package hackathon2024.hackatoons.service;
import com.jme3.anim.AnimComposer;
//import com.jme3.anim.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

import java.util.LinkedList;
import java.util.Queue;

public class ConveyorHumanCall extends SimpleApplication {

    private Node conveyorBeltNode = new Node("ConveyorBelt");
    private Node humanNode = new Node("Human");
    private Queue<Carrier> carriers = new LinkedList<>();
    private Geometry humanHand;
    private boolean isPickingUp = false;
    private Geometry currentHeavyCarrier;
    private float handProgress = 0f;
    private float conveyorSpeed = 2f;
    private AnimComposer animComposer;

    public static void main(String[] args) {
        ConveyorHumanCall app = new ConveyorHumanCall();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initConveyorBelt();
        initCarriers();
        initHuman();

        // Set up the camera
        cam.setLocation(new Vector3f(0, 8, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    private void initConveyorBelt() {
        // Create the conveyor belt
        Box beltShape = new Box(10, 0.1f, 2);
        Geometry conveyorBelt = new Geometry("ConveyorBelt", beltShape);
        Material beltMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beltMaterial.setColor("Color", ColorRGBA.Gray);
        conveyorBelt.setMaterial(beltMaterial);
        conveyorBelt.setLocalTranslation(0, 0, 0);
        conveyorBeltNode.attachChild(conveyorBelt);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void initCarriers() {
        // Create 20 carriers; 5th and 11th are heavy
        for (int i = 0; i < 20; i++) {
            boolean isHeavy = (i == 4 || i == 10);
            carriers.add(createCarrier(i, isHeavy));
        }
    }

    private Carrier createCarrier(int index, boolean isHeavy) {
        // Create a box for the carrier
        Box carrierShape = new Box(0.5f, 0.5f, 0.5f);
        Geometry carrierGeometry = new Geometry("Carrier" + index, carrierShape);
        Material carrierMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        carrierMaterial.setColor("Color", isHeavy ? ColorRGBA.Red : ColorRGBA.Blue);
        carrierGeometry.setMaterial(carrierMaterial);

        // Position the carrier initially off the belt
        carrierGeometry.setLocalTranslation(-10 - index * 2, 0.5f, 0);
        conveyorBeltNode.attachChild(carrierGeometry);

        return new Carrier(carrierGeometry, isHeavy);
    }

    private void initHuman() {
        Spatial human = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        //Spatial robot = assetManager.loadModel()
        human.scale(0.5f);
        human.setLocalTranslation(3, 0, 0); // Position near conveyor belt
        humanNode.attachChild(human);
        rootNode.attachChild(humanNode);

        // Access AnimComposer for animation control
        animComposer = human.getControl(AnimComposer.class);
        System.out.println("Available animation clips: " + animComposer.getAnimClips());
        if (animComposer != null) {
            animComposer.setCurrentAction("HandsRelaxed"); // Set default idle animation
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (isPickingUp) {
            // Handle the human picking up the heavy carrier
            performPickupAnimation(tpf);
            return;
        }

        // Move all carriers on the conveyor belt
        for (Carrier carrier : carriers) {
            carrier.geometry.move(conveyorSpeed * tpf, 0, 0);
        }

        // Check for the heavy carrier reaching the center
        if (!carriers.isEmpty() && !isPickingUp) {
            Carrier carrier = carriers.peek();
            if (carrier.geometry.getLocalTranslation().x >= 0) {
                if (carrier.isHeavy) {
                    // Highlight the heavy carrier and initiate pickup
                    currentHeavyCarrier = carrier.geometry;
                    isPickingUp = true;
                } else {
                    // Remove the carrier from the queue (normal weight)
                    conveyorBeltNode.detachChild(carrier.geometry);
                    carriers.poll();
                }
            }
        }
    }

    private void performPickupAnimation(float tpf) {
        // Trigger "Pick" animation on the human
        if (animComposer != null) {
            animComposer.setCurrentAction("DrawSwords"); // Assume "PickUp" is the animation for picking up objects
        }

        // Move the hand to the heavy carrier
        if (handProgress < 1) {
            currentHeavyCarrier.move(0, tpf * 3, 0); // Lift carrier up
            handProgress += tpf;
        } else {
            // Finish the pickup and remove the carrier
            conveyorBeltNode.detachChild(currentHeavyCarrier);
            carriers.poll();
            currentHeavyCarrier = null;
            isPickingUp = false;
            handProgress = 0;

            // Reset animation to idle
            if (animComposer != null) {
                animComposer.setCurrentAction("Dance");
            }
        }
    }

    // Class to represent each carrier
    private static class Carrier {
        Geometry geometry;
        boolean isHeavy;

        Carrier(Geometry geometry, boolean isHeavy) {
            this.geometry = geometry;
            this.isHeavy = isHeavy;
        }
    }
}

