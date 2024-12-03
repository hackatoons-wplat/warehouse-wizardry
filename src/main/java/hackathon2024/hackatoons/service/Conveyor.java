package hackathon2024.hackatoons.service;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.light.AmbientLight;
import com.jme3.scene.Spatial;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

// Simplified Conveyor Belt code

public class Conveyor extends SimpleApplication {

    public static void main(String[] args) {
        Conveyor app = new Conveyor();
        app.start();
    }
    private void initCarrier(AssetManager assetManager, List<Vector3f> keyframes, float speed) {
        // Load the box-small.obj model for the carrier
        Node carrierNode = new Node("Carrier");
        Spatial carrierModel = assetManager.loadModel("Models/box-small.obj");

        // Scale and position the carrier initially at the first keyframe
        carrierModel.setLocalScale(1f);
        if (keyframes != null && !keyframes.isEmpty()) {
            carrierNode.setLocalTranslation(keyframes.get(0));  // Set initial position to first keyframe
        } else {
            carrierNode.setLocalTranslation(Vector3f.ZERO);  // Default position if no keyframes
        }
        carrierNode.attachChild(carrierModel);
        rootNode.attachChild(carrierNode);

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
                        Vector3f interpolated = new Vector3f().interpolateLocal(start, end, t);

                        // Safely update the carrier position on the main rendering thread
                        enqueue(() -> {
                            carrierNode.setLocalTranslation(interpolated);
                        });

                        Thread.sleep(10);  // Control movement speed (10ms step delay)
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void simpleInitApp() {
        initConveyorBars(assetManager);
        List<Vector3f> keyframesList = Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(5, 0, 0),
                new Vector3f(5, 0, 5),
                new Vector3f(0, 0, 5)
        );

        float speed = 2f;  // Carrier movement speed

// Initialize the carrier and start movement
        initCarrier(assetManager, keyframesList, speed);
        initConveyorBarsVertical(assetManager);
        initConveyorBarsVerticalUp(assetManager);
        addAmbientLighting();
        setupCamera();
    }

    public void initConveyorBars(AssetManager assetManager) {
        Node conveyorBarsNode = new Node("ConveyorBars");  // Node to hold all the conveyor bars

        int numBars = 10;       // Number of bars
        float barSpacing = 0f;  // Space between each bar

        for (int i = 0; i < numBars; i++) {
            // Load the conveyor bar model
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-high.obj");

            // Scale and position each bar along the X-axis
            conveyorBarModel.setLocalScale(1f);
            conveyorBarModel.setLocalTranslation(i - (numBars * barSpacing / 2), 0, 0);  // Centered position

            // Attach the bar to the conveyorBarsNode
            conveyorBarsNode.attachChild(conveyorBarModel);
        }

        // Attach the node with all bars to the rootNode
        rootNode.attachChild(conveyorBarsNode);
    }

    public void initConveyorBarsVertical(AssetManager assetManager) {
        Node conveyorBarsNodeVertical = new Node("ConveyorBarsVert");  // Node to hold all the conveyor bars

        int numBars = 10;       // Number of bars
        float barSpacing = 0f;  // Space between each bar

        for (int i = 0; i < numBars; i++) {
            // Load the conveyor bar model
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-stripe.obj");

            // Scale and position each bar along the X-axis
            conveyorBarModel.setLocalScale(1f);
            conveyorBarModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));
            conveyorBarModel.setLocalTranslation(10,0,i - (numBars * barSpacing / 2));  // Centered position

            // Attach the bar to the conveyorBarsNode
            conveyorBarsNodeVertical.attachChild(conveyorBarModel);
        }

        // Attach the node with all bars to the rootNode
        rootNode.attachChild(conveyorBarsNodeVertical);
    }

    public void initConveyorBarsVerticalUp(AssetManager assetManager) {
        Node conveyorBarsNodeVerticalUp = new Node("ConveyorBarsVertUp");  // Node to hold all the conveyor bars
        int numBars = 10;       // Number of bars
        float barSpacing = 0f;  // Space between each bar
        for (int i = 0; i < numBars; i++) {
            // Load the conveyor bar model
            Node conveyorBarModel = (Node) assetManager.loadModel("Models/conveyor-bars-stripe.obj");
            conveyorBarModel.setLocalScale(1f);
            conveyorBarModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));
            conveyorBarModel.setLocalTranslation(10,0,-1 * (i - (numBars * barSpacing / 2)));
            conveyorBarsNodeVerticalUp.attachChild(conveyorBarModel);
        }
        rootNode.attachChild(conveyorBarsNodeVerticalUp);
    }
    public void addAmbientLighting() {
        // Add ambient light for general illumination
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.8f));  // Adjust brightness if needed
        rootNode.addLight(ambient);
    }

    public void setupCamera() {
        // Position the camera to provide a good view of the conveyor bars
        cam.setLocation(new Vector3f(0, 5, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }
}
