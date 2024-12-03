package hackathon2024.hackatoons.service;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.Arrays;
import java.util.List;

public class Conveyor extends SimpleApplication {

    private static final int MAX_CARRIERS = 10;   // Maximum number of carriers to spawn
    private int carriersSpawned = 0;              // Count of carriers spawned
    private float spawnDelay = 1.0f;              // Delay between spawning carriers in seconds
    private float timeSinceLastSpawn = 0;         // Timer to track time since last spawn

    public static void main(String[] args) {
        Conveyor app = new Conveyor();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initConveyorBars(assetManager);
        initFloor();
        initConveyorBarsVertical(assetManager);
        initConveyorBarsVerticalUp(assetManager);
        addAmbientLighting();
        setupCamera();
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
    }

    private void spawnCarrier() {
        // Keyframes for the carrier movement
        List<Vector3f> keyframes = Arrays.asList(
                new Vector3f(0, 0.25f, 0),
                new Vector3f(5, 0.25f, 0),
                new Vector3f(10, 0.25f, 0),
                new Vector3f(10, 0.25f, -10),
                new Vector3f(10, 0.25f, 10)
        );
        initCarrier(assetManager, keyframes, 2f);
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

        new Thread(() -> {
            try {
                for (int i = 0; i < keyframes.size() - 1; i++) {
                    Vector3f start = keyframes.get(i);
                    Vector3f end = keyframes.get(i + 1);
                    float distance = start.distance(end);
                    float duration = distance / speed;
                    float step = 0.01f;

                    for (float t = 0; t <= 1; t += step / duration) {
                        Vector3f interpolated = new Vector3f().interpolateLocal(start, end, t);
                        enqueue(() -> carrierNode.setLocalTranslation(interpolated));
                        Thread.sleep(10);
                    }
                }
                // Detach the carrier after reaching the last keyframe
                enqueue(() -> rootNode.detachChild(carrierNode));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void initConveyorBars(AssetManager assetManager) {
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
    }
}
