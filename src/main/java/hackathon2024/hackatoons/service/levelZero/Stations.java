package hackathon2024.hackatoons.service.levelZero;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.util.LinkedList;
import java.util.Queue;

public class Stations extends SimpleApplication {

    private Node conveyorBeltNode = new Node("ConveyorBelt");
    private Node stationNode = new Node("Station");
    private Queue<Carrier> carriers = new LinkedList<>();
    private int rotation = 0;

    private float conveyorSpeed = 2f;

    public static void main(String[] args) {
        Stations app = new Stations();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Warehouse Wizardry");
        settings.setResolution(1280, 720);
        settings.setFullscreen(false);
        app.setSettings(settings);
        app.start();
    }

    public void simpleInitApp() {
        initConveyorBelt();
        initCarriers();
        initStation();
        setupInput();
        initSystemExit();
        initStorage();
        initFloor();
        createHorizontalConveyorBelt();
        createVerticalConveyorBelt();
        initCarriers();
        addLighting();  // Add lighting to the scene
        initConveyorBarsHorizontal(); // Initialize the conveyor bars array
        initConveyorBarsVertical(); // Initialize the conveyor bars array
        // Set up the camera
        cam.setLocation(new Vector3f(0, 8, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Set up the camera
        cam.setLocation(new Vector3f(0, 12, 20));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    public void initConveyorBelt() {
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

    public void initStation() {
        Node stationNode = new Node("Station");
        Node station = (Node) assetManager.loadModel("Models/scanner-high.obj"); // Add your .glb file path
        station.setLocalTranslation(-8f, 0, 0); // Position the station at the start of the conveyor belt
        station.scale(2.5f);

        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("Goods Intake");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(-15f, -5f, -5f); // Adjust the position as needed
        stationNode.attachChild(title);


        stationNode.attachChild(station);
        rootNode.attachChild(stationNode);
    }

    public void initCarriers() {
        // Create 20 carriers; 5th and 11th are heavy
        for (int i = 0; i < 1; i++) {
            carriers.add(createCarrier(i));
        }
    }

    public Carrier createCarrier(int index) {
        // Create a box for the carrier
        Box carrierShape = new Box(0.5f, 0.5f, 0.5f);
        Geometry carrierGeometry = new Geometry("Carrier" + index, carrierShape);
        Material carrierMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        carrierMaterial.setColor("Color", ColorRGBA.Brown);
        carrierGeometry.setMaterial(carrierMaterial);

        // Position the carrier initially off the belt
        carrierGeometry.setLocalTranslation(index, 1f, 0);
        conveyorBeltNode.attachChild(carrierGeometry);

        return new Carrier(carrierGeometry);
    }

    @Override
    public void simpleUpdate(float tpf) {

        // Move all carriers on the conveyor belt
        for (Carrier carrier : carriers) {
            carrier.geometry.move(conveyorSpeed * tpf, 0, 0);
        }

        // Check for the heavy carrier reaching the center
        if (!carriers.isEmpty()) {
            Carrier carrier = carriers.peek();
            if (carrier.geometry.getLocalTranslation().x >= 0) {

                    // Remove the carrier from the queue (normal weight)
                    conveyorBeltNode.detachChild(carrier.geometry);
                    carriers.poll();

            }
        }
    }

    // Class to represent each carrier
    public static class Carrier {
        Geometry geometry;

        Carrier(Geometry geometry) {
            this.geometry = geometry;
        }
    }

    private void setupInput() {
        inputManager.addMapping("RightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("LeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "RightClick", "LeftClick");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed && rotation == 0) {
                if (name.equals("RightClick")) {
                    moveCarrier(0, -5, 0); // Move down
                } else if (name.equals("LeftClick")) {
                    moveCarrier(0, 2, 0); // Move up
                }
            }
            ++rotation;
        }
    };

    public void moveCarrier(float x, float y, float z) {
        // Move the first carrier in the queue
        if (!carriers.isEmpty()) {
            Carrier carrier = carriers.peek();
            carrier.geometry.move(x, y, z);
        }
    }

    private void createHorizontalConveyorBelt() {
        // Create the conveyor belt
        Box beltShape = new Box(10, 0.1f, 1);
        Geometry conveyorBelt = new Geometry("ConveyorBelt", beltShape);
        Material beltMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        Texture texture = assetManager.loadTexture("Textures/ConveyorBelt.png"); // Replace with actual texture
        beltMaterial.setTexture("ColorMap", texture);
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
        Texture texture = assetManager.loadTexture("Textures/ConveyorBelt.png"); // Replace with actual texture
        beltMaterial.setTexture("ColorMap", texture);
        conveyorBelt.setMaterial(beltMaterial);
        conveyorBelt.setLocalTranslation(10, 0, 0);
        conveyorBeltNode.attachChild(conveyorBelt);
        rootNode.attachChild(conveyorBeltNode);
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




    private void initStorage(){

        Node storageNode = new Node("Storage");
        Node storage = (Node) assetManager.loadModel("Models/cover-stripe-window.obj"); // Add your .glb file path

        //Spatial storage =  assetManager.loadModel("Models/cover-stripe-window.obj"); // Add your .glb file path
        storage.setLocalTranslation(9.6f, 1f, -19f); // Position the station at the start of the conveyor belt
        storage.rotate(0, FastMath.HALF_PI, 0);
        storage.scale(2f);
        // Create and apply saffron material
       // Material saffronMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //saffronMaterial.setColor("Color", new ColorRGBA(1.0f, 0.6f, 0.2f, 1.0f)); // Saffron color
        //storage.setMaterial(saffronMaterial);

        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("Goods Intake");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(-15f, -5f, -5f); // Adjust the position as needed
        conveyorBeltNode.attachChild(title);


        conveyorBeltNode.attachChild(storage);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void initSystemExit(){
        Node systemExitNode = new Node("SystemExit");
        Node systemExit = (Node) assetManager.loadModel("Models/structure-doorway-wide.obj"); // Add your .glb file path
//        Spatial systemExit = assetManager.loadModel("Models/structure-doorway-wide.obj"); // Add your .glb file path
        systemExit.setLocalTranslation(9.6f, 0f, 18f); // Position the station at the start of the conveyor belt
//        systemExit.rotate(0, FastMath.HALF_PI, 0);
        systemExit.scale(1.5f);


        // Add title "Goods Intake" above the station
//        BitmapText title = new BitmapText(guiFont, false);
//        title.setSize(1f);
//        title.setText("Goods Intake");
//        title.setColor(ColorRGBA.White);
//        title.setLocalTranslation(-15f, -5f, -5f); // Adjust the position as needed
//        stationNode.attachChild(title);


        conveyorBeltNode.attachChild(systemExit);
        rootNode.attachChild(conveyorBeltNode);
    }

}
