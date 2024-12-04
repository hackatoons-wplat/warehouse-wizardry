package hackathon2024.hackatoons.service;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.font.*;
import java.util.Random;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.light.AmbientLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.system.AppSettings;

import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

public class Conveyor extends SimpleApplication {

    private static final int MAX_CARRIERS = 10;   // Maximum number of carriers to spawn
    private int carriersSpawned = 0;              // Count of carriers spawned
    private float spawnDelay = 1.0f;              // Delay between spawning carriers in seconds
    private float timeSinceLastSpawn = 0;         // Timer to track time since last spawn
    private int Score = 0;
    private BitmapText scoreText;
    private boolean clickedRight = false, clickedLeft = false;
    public static void main(String[] args) {

        Conveyor app = new Conveyor();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Warehouse Wizardry");
        settings.setResolution(1280, 720);
        settings.setFullscreen(false);
        app.setSettings(settings);
        app.start();
    }


    List<Vector3f> keyframes = Arrays.asList(
            new Vector3f(0, 0.25f, 0),
            new Vector3f(5, 0.25f, 0),
            new Vector3f(10, 0.25f, 0),
            new Vector3f(10, 0.25f, -10)
            //new Vector3f(10, 0.25f, 10)
    );
    List<Vector3f> keyframes_exit = Arrays.asList(
            new Vector3f(0, 0.25f, 0),
            new Vector3f(5, 0.25f, 0),
            new Vector3f(10, 0.25f, 0),
            //new Vector3f(10, 0.25f, -10),
            new Vector3f(10, 0.25f, 10)
    );

    @Override
    public void simpleInitApp() {

        initConveyorBars(assetManager);
        initSystemExit();
        initStorage();
        initFloor();
        initMouseInput();
        initConveyorBarsVertical(assetManager);
        initConveyorBarsVerticalUp(assetManager);
        addAmbientLighting();
        setupCamera();
        setupInput();  // Initialize input handling for mouse clicks
    }
    private void updateCarrierMovements(float tpf) {
        // Iterate over all carrier nodes in the scene
        for (Spatial carrier : rootNode.getChildren()) {
            if (carrier instanceof Node && carrier.getName().equals("Carrier")) {
                Node carrierNode = (Node) carrier;
                Spatial carrierModel = carrierNode.getChild(0); // Assuming model is the first child

                // Check if the carrier has keyframes to move along
                if (carrierModel != null && carrierModel.getUserData("keyframes") != null) {
                    List<Vector3f> keyframes = (List<Vector3f>) carrierModel.getUserData("keyframes");
                    if (keyframes.size() > 1) {
                        // Update the carrier's position along the keyframe path
                        updateCarrierPositionAlongPath(carrierNode, keyframes, tpf);
                    }
                }
            }
        }
    }

    private void updateCarrierPositionAlongPath(Node carrierNode, List<Vector3f> keyframes, float tpf) {
        // Store or retrieve the carrier's current position along the keyframes path
        Float progress = (Float) carrierNode.getUserData("progress");
        if (progress == null) progress = 0f;  // Default to start if no progress data exists

        // Calculate the current and next keyframe based on progress
        int currentKeyframeIndex = progress.intValue();
        int nextKeyframeIndex = Math.min(currentKeyframeIndex + 1, keyframes.size() - 1);

        Vector3f start = keyframes.get(currentKeyframeIndex);
        Vector3f end = keyframes.get(nextKeyframeIndex);

        // Move the carrier towards the next keyframe
        float step = tpf * 2f; // Movement speed factor (can adjust as needed)
        if (progress < 1f) {
            // Update position
            Vector3f newPosition = new Vector3f().interpolateLocal(start, end, progress);
            carrierNode.setUserData("progress", progress);  // Save progress
        } else {
            // Carrier reached the end of the path, reset progress for next movement
            carrierNode.setUserData("progress", 0f);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Accumulate time since last spawn
        timeSinceLastSpawn += tpf;

        // Check if it's time to spawn a new carrier and we haven't reached the limit
        if (timeSinceLastSpawn >= spawnDelay && carriersSpawned < MAX_CARRIERS) {
            spawnCarrier();
            carriersSpawned++;
            timeSinceLastSpawn = 0;  // Reset the timer
        }

        // Move the carriers along their paths
        updateCarrierMovements(tpf);
    }

    private void spawnCarrier() {
        // Keyframes for the carrier movement
        List<Vector3f> keyframes = Arrays.asList(
                new Vector3f(0, 0.25f, 0),
                new Vector3f(5, 0.25f, 0),
                new Vector3f(10, 0.25f, 0),
                new Vector3f(10, 0.25f, -10)
                //new Vector3f(10, 0.25f, 10)
        );
        List<Vector3f> keyframes_exit = Arrays.asList(
                new Vector3f(0, 0.25f, 0),
                new Vector3f(5, 0.25f, 0),
                new Vector3f(10, 0.25f, 0),
                //new Vector3f(10, 0.25f, -10),
                new Vector3f(10, 0.25f, 10)
        );
        initCarrier(assetManager, keyframes_exit, 2f);
    }

    private void initCarrier(AssetManager assetManager, List<Vector3f> keyframes, float speed) {
        Node carrierNode = new Node("Carrier");
        Spatial carrierModel = assetManager.loadModel("Models/box-small.obj");

        carrierModel.setLocalScale(1f);
        if (keyframes != null && !keyframes.isEmpty()) {
            carrierNode.setLocalTranslation(keyframes.get(0));
        } else {
            carrierNode.setLocalTranslation(Vector3f.ZERO);
        }
        carrierNode.attachChild(carrierModel);
        rootNode.attachChild(carrierNode);

        // Add click listener for the carrier
        carrierModel.setUserData("carrierNode1", carrierNode);

        // Start a thread for carrier movement
        startCarrierMovement(carrierNode, keyframes, speed);
    }

    // Start moving the carrier along keyframes
    private void startCarrierMovement(Node carrierNode, List<Vector3f> keyframes, float speed) {

        new Thread(() -> {
            try {
                Random random = new Random();
                int randomNumber = random.nextInt(2);
                if (randomNumber == 0 ) {
                    for (int i = 0; i < this.keyframes.size() - 1; i++) {
                        Vector3f start = this.keyframes.get(i);
                        Vector3f end = this.keyframes.get(i + 1);
                        float distance = start.distance(end);
                        float duration = distance / speed;
                        float step = 0.01f;

                        for (float t = 0; t <= 1; t += step / duration) {
                            Vector3f interpolated = new Vector3f().interpolateLocal(start, end, t);
                            enqueue(() -> carrierNode.setLocalTranslation(interpolated));
                            Thread.sleep(10);
                        }
                    }
                    Score+=100;
                }
                    else {

                    Material redMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    redMaterial.setColor("Color", ColorRGBA.Red);  // Set red color
                    redMaterial.setColor("GlowColor", ColorRGBA.Red);  // Optional: Add a glow effect

                    // Apply the material to the spatial
                    carrierNode.setMaterial(redMaterial);

                    //Node carrierNodeNew = new Node("CarrierFailed");
                    //carrierNode.detachChild(carrierNode.getChild());
                    //carrierNodeNew.attachChild(assetManager.loadModel("Models/box-large.obj"));
                    //rootNode.attachChild(carrierNodeNew);

                        for (int i = 0; i < this.keyframes_exit.size() - 1; i++) {
                            Vector3f start = this.keyframes_exit.get(i);
                            Vector3f end = this.keyframes_exit.get(i + 1);
                            float distance = start.distance(end);
                            float duration = distance / speed;
                            float step = 0.01f;

                            for (float t = 0; t <= 1; t += step / duration) {
                                Vector3f interpolated = new Vector3f().interpolateLocal(start, end, t);
                                enqueue(() -> carrierNode.setLocalTranslation(interpolated));
                                Thread.sleep(10);
                            }
                        }
                    Score -= 100;
                    }

                // Detach the carrier after reaching the last keyframe
                enqueue(() -> rootNode.detachChild(carrierNode));
                    scoreText.setText("Score: " + Score);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // This will be called by the input handler on mouse click
    public void onClickCarrier(Node carrierNode) {
        System.out.println("Carrier Should be deleted");
        //rootNode.detachChild(carrierNode);  // Detach the clicked carrier
        enqueue(() -> rootNode.detachChild(carrierNode));
    }

    public void initConveyorBars(AssetManager assetManager) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        scoreText = new BitmapText(font, false);
        scoreText.setSize(20);
        scoreText.setColor(ColorRGBA.Green);
        scoreText.setText("Score: 0");
        scoreText.setLocalTranslation(settings.getWidth() - 200, 50, 0);
        guiNode.attachChild(scoreText);

        Node conveyorBarsNode = new Node("ConveyorBars");
        int numBars = 10;
        float barSpacing = 0f;

        for (int i = 0; i < numBars; i++) {
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-high.obj");
            conveyorBarModel.setLocalScale(1f);
            conveyorBarModel.setLocalTranslation(i - (numBars * barSpacing / 2), 0, 0);
            conveyorBarsNode.attachChild(conveyorBarModel);
        }

        rootNode.attachChild(conveyorBarsNode);
    }

    public void initConveyorBarsVertical(AssetManager assetManager) {
        Node conveyorBarsNodeVertical = new Node("ConveyorBarsVert");
        int numBars = 10;
        float barSpacing = 0f;

        for (int i = 0; i < numBars; i++) {
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-stripe.obj");
            conveyorBarModel.setLocalScale(1f);
            conveyorBarModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));
            conveyorBarModel.setLocalTranslation(10, 0, i - (numBars * barSpacing / 2));
            conveyorBarsNodeVertical.attachChild(conveyorBarModel);
        }

        rootNode.attachChild(conveyorBarsNodeVertical);
    }

    public void initConveyorBarsVerticalUp(AssetManager assetManager) {
        Node conveyorBarsNodeVerticalUp = new Node("ConveyorBarsVertUp");
        int numBars = 10;
        float barSpacing = 0f;

        for (int i = 0; i < numBars; i++) {
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-stripe.obj");
            conveyorBarModel.setLocalScale(1f);
            conveyorBarModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));
            conveyorBarModel.setLocalTranslation(10, 0, -1 * (i - (numBars * barSpacing / 2)));
            conveyorBarsNodeVerticalUp.attachChild(conveyorBarModel);
        }
        rootNode.attachChild(conveyorBarsNodeVerticalUp);
    }

    public void addAmbientLighting() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(ambient);
    }

    private void initFloor() {
        int gridSize = 10;
        float tileSize = 10f;
        Node floorNode = new Node("Floor");

        for (int x = -gridSize; x < gridSize; x++) {
            for (int z = -gridSize; z < gridSize; z++) {
                Node floorTile = (Node) assetManager.loadModel("Models/floor-large.obj");
                floorTile.setLocalScale(4.8f);
                floorTile.setLocalTranslation(x * tileSize, -0.1f, z * tileSize);
                floorNode.attachChild(floorTile);
            }
        }

        rootNode.attachChild(floorNode);
    }

    public void setupCamera() {
        cam.setLocation(new Vector3f(0, 5, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        setDisplayFps(false);
        setDisplayStatView(false);
        flyCam.setEnabled(true);  // Disable camera movement (for mouse clicking only)
    }

    // Set up input handling for mouse clicks
    private void setupInput() {
    }

    // ActionListener for mouse click events


    private void initStorage(){

        Node conveyorBeltNode = new Node("ConveyorBelt");
        Node storage = (Node) assetManager.loadModel("Models/cover-stripe-window.obj"); // Add your .glb file path

        storage.setLocalTranslation(10f, 0f, -10f); // Position the station at the start of the conveyor belt
        storage.rotate(0, FastMath.HALF_PI, 0);
        storage.scale(1f);

        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("Stock Room");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(10f, 3f, -10); // Adjust the position as needed
        conveyorBeltNode.attachChild(title);


        conveyorBeltNode.attachChild(storage);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void initSystemExit(){

        Node stationNode = new Node("Station");
        Node conveyorBeltNode = new Node("ConveyorBelt");
        Node systemExit = (Node) assetManager.loadModel("Models/cover-window.obj");
        systemExit.rotate(0, -FastMath.HALF_PI, 0);
        systemExit.setLocalTranslation(10f, 0f, 10f);
        systemExit.scale(1f);


        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("ExitDock");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(9f, 2f, 10); // Adjust the position as needed
        title.rotate(0,-FastMath.HALF_PI,0);
        conveyorBeltNode.attachChild(title);
        conveyorBeltNode.attachChild(systemExit);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void initMouseInput() {

        // Map mouse buttons to actions

        inputManager.addMapping("StopConveyor", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)); // Right click

        inputManager.addMapping("ResumeConveyor", new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // Left click

        // Add listeners for the actions

        inputManager.addListener(actionListener, "StopConveyor", "ResumeConveyor");

    }

    private ActionListener actionListener = (name, isPressed, tpf) -> {

        if (name.equals("StopConveyor") && !isPressed) {
            System.out.println("Here");


        }

        if (name.equals("ResumeConveyor") && !isPressed) {

            System.out.println("Here left");



        }

    };

}
