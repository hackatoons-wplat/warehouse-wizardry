package hackathon2024.hackatoons.service;

import com.jme3.asset.AssetManager;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.FastMath;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BoxManager {

    private int score = 0; // Track the score
    private BitmapText scoreText; // Text element to display the score

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

                // Randomly choose the box model and path based on size
                String boxModel = boxModels.get(random.nextInt(boxModels.size()));
                List<Vector3f> path = (boxModel.contains("small")) ? accepted : rejected;
                System.out.println("Spawning box: " + boxModel);
                spawnAndAnimateBox(path, boxModel);

                // Introduce a delay of 1 second (1000 milliseconds) before spawning the next box
                try {
                    Thread.sleep(1000);  // 1 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void spawnAndAnimateBox(List<Vector3f> path, String boxModel) {
        Node boxNode = (Node) assetManager.loadModel(boxModel);  // Load box model

        boxNode.setLocalScale(0.6f + random.nextFloat() * 0.2f); // Set a random scale for the box
        boxNode.setLocalTranslation(path.get(0)); // Set the starting position

        // Add the box to the scene
        rootNode.attachChild(boxNode);

        // Create a MotionPath for the box
        MotionPath motionPath = new MotionPath();

        // Add waypoints (positions) to the motion path
        motionPath.addWayPoint(path.get(0)); // Starting point (0, 0.25, 0)
        motionPath.addWayPoint(new Vector3f(5, 0.25f, 0)); // Intermediate point (5, 0.25, 0)
        motionPath.addWayPoint(new Vector3f(10, 0.25f, 0)); // Stop point (10, 0.25, 0)

        // Randomly choose the final destination
        Vector3f finalDestination = (path == rejected) ? new Vector3f(10, 0.25f, 10) : new Vector3f(10, 0.25f, -10);
        motionPath.addWayPoint(finalDestination); // Final destination

        // Create a MotionEvent to move the box along the path
        MotionEvent motionEvent = new MotionEvent(boxNode, motionPath);
        motionEvent.setSpeed(1f); // Slower speed for less fluidity
        motionEvent.setDirectionType(MotionEvent.Direction.Path); // Avoid rotation while moving

        // Set a listener to manage the pause and remove the box once the animation ends
        motionPath.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent control, int wayPointIndex) {
                if (wayPointIndex == 2) {
                    // At the (10, 0.25, 0) point, stop the box for 3 seconds
                    triggerCooldown(); // Start cooldown after the stop
                } else if (wayPointIndex == motionPath.getNbWayPoints() - 1) {
                    // After the final destination, remove the box
                    rootNode.detachChild(boxNode);
                    resetCooldown(); // Reset cooldown after the box reaches the final destination
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
                Thread.sleep(10000); // Wait for 3 seconds (stop at (10, 0.25, 0))
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isCooldown = false; // Reset cooldown after 3 seconds
        }).start();
    }

    // Reset cooldown flag
    private void resetCooldown() {
        isCooldown = false;
    }
}
