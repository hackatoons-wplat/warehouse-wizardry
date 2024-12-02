package hackathon2024.hackatoons.service;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.system.AppSettings;

import java.util.LinkedList;
import java.util.Queue;

public class ConveyorHand extends SimpleApplication {

    private Node conveyorBeltNode = new Node("ConveyorBelt");
    private Node handNode = new Node("Hand");
    private Queue<Carrier> carriers = new LinkedList<>();
    private Geometry humanHand;
    private boolean isPickingUp = false;
    private Geometry currentHeavyCarrier;
    private float handProgress = 0f;

    private float conveyorSpeed = 2f;

    public static void main(String[] args) {
        ConveyorHand app = new ConveyorHand();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Warehouse Wizardry");
        settings.setResolution(1280, 720);
        settings.setFullscreen(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Disable default camera movement (keyboard and mouse drag)
        flyCam.setEnabled(false);  // Disables the fly camera control

        // Set camera to a fixed position and orientation
        cam.setLocation(new Vector3f(0, 14, 25)); // Fixed camera position
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y); // Camera fixed to look at the origin

        // Enable mouse cursor
        setDisplayStatView(false);  // Hides stats
        setDisplayFps(false);  // Hides FPS
        inputManager.setCursorVisible(true);  // Makes the cursor visible

        // Continue with your other initialization
        initFloor();
        createHorizontalConveyorBelt();
        createVerticalConveyorBelt();
        initCarriers();
        initHand();
        addLighting();  // Add lighting to the scene
        initConveyorBarsHorizontal(); // Initialize the conveyor bars array
        initConveyorBarsVertical(); // Initialize the conveyor bars array
    }

    private void createHorizontalConveyorBelt() {
        // Create the conveyor belt
        Box beltShape = new Box(10, 0.1f, 1);
        Geometry conveyorBelt = new Geometry("ConveyorBelt", beltShape);
        Material beltMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        //Texture texture = assetManager.loadTexture("Textures/ConveyorBelt.png"); // Replace with actual texture
        //beltMaterial.setTexture("ColorMap", texture);
        conveyorBelt.setMaterial(beltMaterial);
        conveyorBelt.setLocalTranslation(0, 0, 0);
        conveyorBeltNode.attachChild(conveyorBelt);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void createVerticalConveyorBelt() {
        // Create the conveyor belt
        Box beltShape = new Box(1, 0.1f, 20);
        Geometry conveyorBelt = new Geometry("ConveyorBelt", beltShape);
        Material beltMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //Texture texture = assetManager.loadTexture("Textures/ConveyorBelt.png"); // Replace with actual texture
        //beltMaterial.setTexture("ColorMap", texture);
        conveyorBelt.setMaterial(beltMaterial);
        conveyorBelt.setLocalTranslation(10, 0, 0);
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
        Node carrierNode = new Node("Carrier" + index);
        Geometry carrierGeometry = null;

        if (isHeavy) {
            // Load the OBJ model for the heavy carrier (box-large.obj)
            Node carrierModel = (Node) assetManager.loadModel("Models/box-large.obj");

            // Access the geometry from the model (assuming there is one)
            carrierGeometry = (Geometry) carrierModel.getChild(0); // Assuming the model has a single geometry

            // Apply scale and translation if needed
            carrierGeometry.setLocalScale(2f);
            carrierGeometry.setLocalTranslation(0, 0.01f, 0);

            // Attach the geometry to the carrier node
            carrierNode.attachChild(carrierGeometry);

        } else {
            // Load the OBJ model for the normal (blue) carrier (box-small.obj)
            Node carrierModel = (Node) assetManager.loadModel("Models/box-small.obj");

            // Access the geometry from the model (assuming there is one)
            carrierGeometry = (Geometry) carrierModel.getChild(0); // Assuming the model has a single geometry

            // Apply scale and translation if needed
            carrierGeometry.setLocalScale(2f);
            carrierGeometry.setLocalTranslation(8, 0.01f, 0);

            // Attach the geometry to the carrier node
            carrierNode.attachChild(carrierGeometry);
        }

        // Position the carrier initially off the belt
        carrierNode.setLocalTranslation(-10 - index * 2, 0.01f, 0);

        // Attach the carrier node to the conveyor belt
        conveyorBeltNode.attachChild(carrierNode);

        return new Carrier(carrierNode, isHeavy);
    }

    private void initFloor() {
        int gridSize = 10; // Number of tiles along one direction
        float tileSize = 10f; // Size of each tile (match with OBJ model dimensions if required)

        Node floorNode = new Node("Floor");

        // Loop over all quadrants (X: negative, positive; Z: negative, positive)
        for (int x = -gridSize; x < gridSize; x++) {
            for (int z = -gridSize; z < gridSize; z++) {
                // Load the floor tile model
                Node floorTile = (Node) assetManager.loadModel("Models/floor-large.obj");
                floorTile.setLocalScale(4.8f); // Scale if necessary
                // Position tiles in all quadrants
                floorTile.setLocalTranslation(x * tileSize, -0.1f, z * tileSize); // Position tiles
                floorNode.attachChild(floorTile);
            }
        }

        rootNode.attachChild(floorNode);
    }

    private void initHand() {
        // Create a simple "hand" to pick up the heavy carrier
        Box handShape = new Box(1, 0.1f, 1);
        humanHand = new Geometry("Hand", handShape);
        Material handMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        handMaterial.setColor("Diffuse", ColorRGBA.Brown);
        handMaterial.setBoolean("UseMaterialColors", true);
        humanHand.setMaterial(handMaterial);

        // Position the hand above the conveyor belt
        humanHand.setLocalTranslation(0, 3, 0);
        handNode.attachChild(humanHand);
        rootNode.attachChild(handNode);


        
    }

    private void addLighting() {
        // Add directional light (simulating sunlight)
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        // Add ambient light for general illumination
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.5f));  // Set the intensity of the ambient light
        rootNode.addLight(ambient);
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
            carrier.move(conveyorSpeed * tpf, 0, 0);
        }

        // Check for the heavy carrier reaching the center
        if (!carriers.isEmpty() && !isPickingUp) {
            Carrier carrier = carriers.peek();
            if (carrier.getLocalTranslation().x >= 0) {
                if (carrier.isHeavy) {
                    // Highlight the heavy carrier and initiate pickup
                    currentHeavyCarrier = (Geometry) carrier.node.getChild(0);
                    isPickingUp = true;
                } else {
                    // Remove the carrier from the queue (normal weight)
                    conveyorBeltNode.detachChild(carrier.node);
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
        }
    }

    private void initConveyorBarsHorizontal() {
        int numBars = 5; // Number of conveyor bars
        float barSpacing = 2f; // Space between each conveyor bar
        float barSize = 2f; // Size of each conveyor bar (adjust if necessary)

        Node conveyorBarsNode = new Node("ConveyorBars"); // Node to hold all the bars

        for (int i = -5; i < numBars; i++) {
            // Load the conveyor bar model
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-high.obj");

            // Scale the model if needed (you can adjust scale)
            conveyorBarModel.setLocalScale(barSize);

            // Set the position of each bar in the one-dimensional array
            conveyorBarModel.setLocalTranslation(i * barSpacing, 0, 0); // Spaced along the x-axis

            // Attach the model to the conveyorBarsNode
            conveyorBarsNode.attachChild(conveyorBarModel);
        }

        // Attach the conveyorBarsNode to the rootNode or any other parent node
        rootNode.attachChild(conveyorBarsNode);
    }

    private void initConveyorBarsVertical() {
        int numBars = 10; // Number of conveyor bars
        float barSpacing = 2f; // Space between each conveyor bar
        float barSize = 2f; // Size of each conveyor bar (adjust if necessary)

        Node conveyorBarsNode = new Node("ConveyorBars"); // Node to hold all the bars

        for (int i = -10; i < numBars; i++) {
            // Load the conveyor bar model
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-high.obj");

            // Scale the model if needed (you can adjust scale)
            conveyorBarModel.setLocalScale(barSize);

            // Apply a 90-degree rotation around the Z-axis
            conveyorBarModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));

            // Set the position of each bar in the one-dimensional array (now spaced along the y-axis)
            conveyorBarModel.setLocalTranslation(10, 0,i * barSpacing); // Spaced along the y-axis due to rotation

            // Attach the model to the conveyorBarsNode
            conveyorBarsNode.attachChild(conveyorBarModel);
        }

        // Attach the conveyorBarsNode to the rootNode or any other parent node
        rootNode.attachChild(conveyorBarsNode);
    }


    // Carrier class to manage each carrier
    private static class Carrier {
        Node node;
        boolean isHeavy;

        Carrier(Node node, boolean isHeavy) {
            this.node = node;
            this.isHeavy = isHeavy;
        }

        public Vector3f getLocalTranslation() {
            return node.getLocalTranslation();
        }

        public void move(float x, float y, float z) {
            node.move(x, y, z);
        }
    }
}
