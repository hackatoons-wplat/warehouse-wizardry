package hackathon2024.hackatoons.service.levelZero;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import hackathon2024.hackatoons.service.Conveyor;
import hackathon2024.hackatoons.service.LoadingWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Stations extends SimpleApplication {

    private Node conveyorBeltNode = new Node("ConveyorBelt");
    private Node stationNode = new Node("Station");
    private Queue<Carrier> carriers = new LinkedList<>();
    private float verticalPoint = 0;
    private boolean isPaused = false;
    private boolean isBoxMoved = false;
    private List<Vector3f> keyframesList = new ArrayList<>();
    private BitmapText tooltip;
    private AudioNode audioNode;

    private float conveyorSpeed = 2f;

    public static void main(String[] args) {
        Stations app = new Stations();
        AppSettings settings = new AppSettings(true);

        settings.setTitle("Warehouse Wizardry");
        settings.setSamples(8);
        settings.setResolution(1280, 720);
        settings.setFullscreen(false);
        app.setSettings(settings);
        app.start();
    }

    public void simpleInitApp() {
         initQuitButton();

        levelUp();
        // Initialize the audio node
        audioNode = new AudioNode(assetManager, "Sounds/Damtaro-Start-_freetouse.com_.wav", AudioData.DataType.Stream);
        audioNode.setLooping(true);  // Loop the audio
        audioNode.setPositional(false);  // Non-positional audio
        audioNode.setVolume(0.5f);  // Set volume
        rootNode.attachChild(audioNode);

        // Play the audio
        audioNode.play();
        keyframesList = Arrays.asList(
                new Vector3f(0, 0.6f, 0),
                new Vector3f(5, 0.6f, 0),
                new Vector3f(5, 0.6f, 0),
                new Vector3f(10, 0.6f, 0),
                new Vector3f(10, 0.6f, 0),
                new Vector3f(10, 0.6f, 5),
                new Vector3f(10, 0.6f, 10),
                new Vector3f(10, 0.6f, 15),
                new Vector3f(10, 0.6f, 20)

        );

        float speed = 2f;  // Carrier movement speed

        initCarrier(assetManager, keyframesList, speed);
        initStation();

        initSystemExit();
        initStorage();
        initFloor();
        createHorizontalConveyorBelt();
        createVerticalConveyorBelt();


        addLighting();  // Add lighting to the scene
        initConveyorBarsHorizontal(); // Initialize the conveyor bars array
        initConveyorBarsVertical(); // Initialize the conveyor bars array
        // Set up the camera
        cam.setLocation(new Vector3f(0, 8, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setEnabled(false);
        // Set up the camera
        cam.setLocation(new Vector3f(0, 12, 20));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    public void initStation() {
        Node station = (Node) assetManager.loadModel("Models/scanner-high.obj"); // Add your .glb file path
        station.setLocalTranslation(-8f, 0, 0); // Position the station at the start of the conveyor belt
        station.scale(2.5f);

        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(0.8f);
        title.setText("Receiving Dock");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(-9f, 5f, 0); // Adjust the position as needed
        stationNode.attachChild(title);


        stationNode.attachChild(station);
        rootNode.attachChild(stationNode);
    }

    public void initCarriers() {
        // Create 20 carriers; 5th and 11th are heavy
        for (int i = 0; i < 10; i++) {
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

        verticalPoint = tpf;

        // Move all carriers on the conveyor belt
        for (Carrier carrier : carriers) {
            carrier.geometry.move(conveyorSpeed * tpf, 0, 0);
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
        inputManager.addMapping("LeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("RightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(actionListener, "LeftClick", "RightClick", "Pause");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed) { // Trigger action only on key/button release
                if (!isBoxMoved) {
                    if (name.equals("LeftClick")) {
                        moveUpward(tpf); // Move the carrier upward
                        synchronized (pauseLock) {
                            isBoxMoved = true; // Mark as moved
                            pauseLock.notify(); // Resume the thread
                        }
                    } else if (name.equals("RightClick")) {
                        moveDownward(); // Move the carrier downward
                        synchronized (pauseLock) {
                            isBoxMoved = true; // Mark as moved
                            pauseLock.notify(); // Resume the thread
                        }
                    }
                }

                // Toggle pause state for the carrier
                if (name.equals("Pause")) {
                    synchronized (pauseLock) {
                        isPaused = !isPaused; // Toggle paused state
                        if (!isPaused) {
                            pauseLock.notify(); // Resume the thread if unpaused
                        }
                    }
                }
            }
        }
    };

    private void moveUpward(float tpf) {
            // Define the action to move upward
            keyframesList.set(4, new Vector3f(10, 0.6f, -5));
            keyframesList.set(5, new Vector3f(10, 0.6f, -10));
            keyframesList.set(6, new Vector3f(10, 0.6f, -15));
            keyframesList.set(7, new Vector3f(10, 0.6f, -20));
            keyframesList.set(8, new Vector3f(10, 0.6f, -22));
    }

    private void moveDownward() {
            keyframesList.set(4, new Vector3f(10, 0.6f, 5));
            keyframesList.set(5, new Vector3f(10, 0.6f, 10));
            keyframesList.set(6, new Vector3f(10, 0.6f, 15));
            keyframesList.set(7, new Vector3f(10, 0.6f, 20));
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
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(0.8f);
        title.setText("Transport Belt");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(-2f, 1f, -3f);
        conveyorBeltNode.attachChild(title);
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

        Node storage = (Node) assetManager.loadModel("Models/cover-stripe-window.obj"); // Add your .glb file path

        storage.setLocalTranslation(9.6f, 1f, -19f); // Position the station at the start of the conveyor belt
        storage.rotate(0, FastMath.HALF_PI, 0);
        storage.scale(2f);

        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("Stock Room");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(8f, 5f, -21f); // Adjust the position as needed
        conveyorBeltNode.attachChild(title);


        conveyorBeltNode.attachChild(storage);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void initSystemExit(){
        Node systemExit = (Node) assetManager.loadModel("Models/structure-doorway-wide.obj");
        systemExit.setLocalTranslation(9.6f, 0f, 18f);
        systemExit.scale(1.5f);


        // Add title "Goods Intake" above the station
        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(0.7f);
        title.setText("Exit Dock");
        title.rotate(0,-FastMath.HALF_PI,0);
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(8f, 3f, 18f); // Adjust the position as needed
        stationNode.attachChild(title);


        conveyorBeltNode.attachChild(systemExit);
        rootNode.attachChild(conveyorBeltNode);
    }

    private final Object pauseLock = new Object();
    private void initCarrier(AssetManager assetManager, List<Vector3f> keyframes, float speed) {
        // Load the box-small.obj model for the carrier
        Node carrierNode = new Node("Carrier");
        Spatial carrierModel = assetManager.loadModel("Models/box-large.obj");

        // Scale and position the carrier initially at the first keyframe
        carrierModel.setLocalScale(1.5f);
        if (keyframes != null && !keyframes.isEmpty()) {
            carrierNode.setLocalTranslation(keyframes.get(0));  // Set initial position to first keyframe
        } else {
            carrierNode.setLocalTranslation(Vector3f.ZERO);  // Default position if no keyframes
        }
        carrierNode.attachChild(carrierModel);
        rootNode.attachChild(carrierNode);

        tooltip = new BitmapText(guiFont, false);
        tooltip.setSize(.5f);
        tooltip.setText("Click for Action");
        tooltip.setColor(ColorRGBA.White);
        tooltip.setLocalTranslation(7, 5, 5);  // Adjust position relative to the carrier
        tooltip.setCullHint(Spatial.CullHint.Always);
        rootNode.attachChild(tooltip);

        // Movement logic
        new Thread(() -> {
            try {
                for (int i = 0; i < keyframes.size() - 1; i++) {
                    Vector3f start = keyframes.get(i);
                    Vector3f end = keyframes.get(i + 1);

                    float distance = start.distance(end);
                    float duration = distance / speed;
                    float step = 0.01f;  // Smaller step for smoother interpolation

                    for (float t = 0; t <= 1; t += step / duration) {

                        Vector3f interpolated = new Vector3f()
                                .set(start.x, start.y, start.z)
                                .interpolateLocal(new Vector3f(end.x, start.y, end.z), t);

                        // Safely update the carrier position on the main rendering thread
                        enqueue(() -> carrierNode.setLocalTranslation(interpolated));

                        // Pause at specific condition
                        if (interpolated.x == 10f && interpolated.y == 0.6f && interpolated.z == 0f) {
                            isBoxMoved = false; // Enable RightClick and LeftClick functionality
                            isPaused = true;   // Indicate that movement is paused
                            setupInput();      // Setup input listeners

                            enqueue(() -> tooltip.setCullHint(Spatial.CullHint.Never));  // Show tooltip
                            Thread.sleep(3000);
                            enqueue(() -> tooltip.setCullHint(Spatial.CullHint.Always));  // Hide tooltip
                            isPaused = false;
                        }

                        Thread.sleep(10);  // Control movement speed (10ms step delay)
                    }
                }
                enqueue(() -> rootNode.detachChild(carrierNode));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initQuitButton() {
        BitmapText quitButton = new BitmapText(guiFont, false);

        quitButton.setSize(guiFont.getCharSet().getRenderedSize() * 3);
        quitButton.setText("Quit");
        quitButton.setColor(ColorRGBA.Red);
        quitButton.setLocalTranslation(10, quitButton.getLineHeight() + 10, 0);  // Position the button at the bottom left corner
        guiNode.attachChild(quitButton);

        inputManager.addMapping("Quit", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Quit") && !isPressed) {
                    Vector2f click2d = inputManager.getCursorPosition();
                    float x = click2d.x;
                    float y = click2d.y;
                    float buttonX = quitButton.getLocalTranslation().x;
                    float buttonY = quitButton.getLocalTranslation().y;
                    float buttonWidth = quitButton.getLineWidth();
                    float buttonHeight = quitButton.getLineHeight();

                    if (x >= buttonX && x <= buttonX + buttonWidth && y >= buttonY - buttonHeight && y <= buttonY) {
                        stop();  // Close the application
                    }
                }
            }
        }, "Quit");
    }
    private void levelUp() {
        BitmapText quitButton = new BitmapText(guiFont, false);

        quitButton.setSize(guiFont.getCharSet().getRenderedSize() * 3);
        quitButton.setText("Level Up");
        quitButton.setColor(ColorRGBA.Green);
        quitButton.setLocalTranslation(settings.getWidth() - quitButton.getLineWidth() - 10, quitButton.getLineHeight() + 10, 0);  // Position the button at the bottom right corner
        guiNode.attachChild(quitButton);

        inputManager.addMapping("Level up", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Level up") && !isPressed) {
                    Conveyor app = new Conveyor();
                    AppSettings settings = new AppSettings(true);
                    settings.setTitle("Warehouse Wizardry");
                    settings.setResolution(1280, 720);
                    settings.setFullscreen(false);
                    app.setSettings(settings);
                    audioNode.stop();
                    app.start();
                }
            }
        }, "Level up");
    }
}

