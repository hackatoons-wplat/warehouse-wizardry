package hackathon2024.hackatoons.service.levelZero;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.LinkedList;
import java.util.Queue;

public class Stations extends SimpleApplication {

    private Node conveyorBeltNode = new Node("ConveyorBelt");
    private Node stationNode = new Node("Station");
    private Node handNode = new Node("Hand");
    private Queue<Carrier> carriers = new LinkedList<>();
    private Geometry humanHand;
    private boolean isPickingUp = false;
    private Geometry currentHeavyCarrier;
    private float handProgress = 0f;

    private float conveyorSpeed = 2f;

    public static void main(String[] args) {
        Stations app = new Stations();
        app.start();
    }

    public void simpleInitApp() {
        initConveyorBelt();
        initCarriers();
        initHand();
        initStation();

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

    private void initStation() {
        // Create the station
        Box stationShape = new Box(1, 1, 1);
        Geometry station = new Geometry("Station", stationShape);
        Material stationMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        stationMaterial.setColor("Color", ColorRGBA.Green);
        station.setMaterial(stationMaterial);
        station.setLocalTranslation(-12, 0.5f, 0); // Position the station at the start of the conveyor belt
        stationNode.attachChild(station);
        rootNode.attachChild(stationNode);
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

    private void initHand() {
        // Create a simple "hand" to pick up the heavy carrier
        Box handShape = new Box(1, 0.1f, 1);
        humanHand = new Geometry("Hand", handShape);
        Material handMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        handMaterial.setColor("Color", ColorRGBA.Brown);
        humanHand.setMaterial(handMaterial);

        // Position the hand above the conveyor belt
        humanHand.setLocalTranslation(0, 3, 0);
        handNode.attachChild(humanHand);
        rootNode.attachChild(handNode);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (isPickingUp) {
            // Handle the hand picking up the heavy carrier
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
        // Move the hand down to pick up the heavy carrier
        if (handProgress < 1) {
            humanHand.move(0, -tpf * 3, 0);
            handProgress += tpf;
        } else if (handProgress < 2) {
            // Lift the heavy carrier upwards
            currentHeavyCarrier.move(0, tpf * 3, 0);
            humanHand.move(0, tpf * 3, 0);
            handProgress += tpf;
        } else {
            // Finish the pickup and remove the carrier
            conveyorBeltNode.detachChild(currentHeavyCarrier);
            carriers.poll();
            currentHeavyCarrier = null;
            isPickingUp = false;
            handProgress = 0;

            // Reset hand position
            humanHand.setLocalTranslation(0, 3, 0);
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



