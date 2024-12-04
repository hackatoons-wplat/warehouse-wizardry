package hackathon2024.hackatoons.service;

import com.jme3.asset.AssetManager;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.FastMath;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BoxManager {

    private final AssetManager assetManager;
    private final Node rootNode;
    private final Random random = new Random();
    private final List<String> boxModels = Arrays.asList(
            "Models/box-large.obj",
            "Models/box-wide.obj",
            "Models/box-small.obj",
            "Models/box-long.obj"
    );

    private final List<Vector3f> accepted;
    private final List<Vector3f> rejected;

    private boolean isCooldown = false; // Flag for cooldown

    public BoxManager(AssetManager assetManager, Node rootNode, List<Vector3f> accepted, List<Vector3f> rejected) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.accepted = accepted;
        this.rejected = rejected;
    }

    public void spawnAndAnimateBoxes(int count) {
        // Start a new thread to simulate a delay between spawns
        new Thread(() -> {
            for (int i = 0; i < count; i++) {
                if (isCooldown) {
                    try {
                        Thread.sleep(1000);  // Wait for cooldown to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                boolean isAccepted = random.nextBoolean(); // Randomly choose path
                List<Vector3f> path = isAccepted ? accepted : rejected;
                spawnAndAnimateBox(path);

                // Introduce a delay of 1 second (1000 milliseconds) before spawning the next box
                try {
                    Thread.sleep(1000);  // 1 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void spawnAndAnimateBox(List<Vector3f> path) {
        String boxModel = boxModels.get(random.nextInt(boxModels.size())); // Random box model
        Node boxNode = (Node) assetManager.loadModel(boxModel);           // Load box model

        boxNode.setLocalScale(0.6f + random.nextFloat() * 0.2f); // Reduce random size to make it less fluid
        boxNode.setLocalTranslation(path.get(0)); // Start at the first position

        // Add box to the scene
        rootNode.attachChild(boxNode);

        // Create a MotionPath for the box
        MotionPath motionPath = new MotionPath();

        // Add straight waypoints (positions) from the path list
        // Use the existing path positions, but avoid smoothing
        motionPath.addWayPoint(path.get(0));
        for (int i = 1; i < path.size(); i++) {
            Vector3f current = path.get(i);
            motionPath.addWayPoint(current);
        }

        // Add the stop at (10, 0.25, 0) for 3 seconds
        motionPath.addWayPoint(new Vector3f(10, 0.25f, 0)); // Keyframe where the box will stop
        motionPath.addWayPoint(path.get(path.size() - 1));  // Add the final destination

        // Create a MotionEvent to move the box along the path
        MotionEvent motionEvent = new MotionEvent(boxNode, motionPath);
        motionEvent.setSpeed(1f); // Slower speed for reduced fluidity
        motionEvent.setDirectionType(MotionEvent.Direction.Path); // Avoid rotation while turning

        // Set a listener to remove the box once the animation ends
        motionPath.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent control, int wayPointIndex) {
                if (wayPointIndex == motionPath.getNbWayPoints() - 1) {
                    rootNode.detachChild(boxNode); // Remove the box once it reaches the end
                    resetCooldown(); // Reset cooldown after box reaches the end
                } else if (wayPointIndex == 1) {
                    // This is the stop point at (10, 0.25, 0)
                    triggerCooldown(); // Start cooldown after the box stops
                }
            }
        });

        // Start the animation
        motionEvent.play();
    }

    // Start the cooldown period (box stop time)
    private void triggerCooldown() {
        isCooldown = true;
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait for 3 seconds (stop at (10, 0.25, 0))
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isCooldown = false; // Reset cooldown
        }).start();
    }

    // Reset cooldown flag
    private void resetCooldown() {
        isCooldown = false;
    }
}
