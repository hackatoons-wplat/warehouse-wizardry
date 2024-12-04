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

    public BoxManager(AssetManager assetManager, Node rootNode, List<Vector3f> accepted, List<Vector3f> rejected) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.accepted = accepted;
        this.rejected = rejected;
    }

    public void spawnAndAnimateBoxes(int count) {
        for (int i = 0; i < count; i++) {
            boolean isAccepted = random.nextBoolean(); // Randomly choose path
            List<Vector3f> path = isAccepted ? accepted : rejected;
            spawnAndAnimateBox(path);
        }
    }

    private void spawnAndAnimateBox(List<Vector3f> path) {
        String boxModel = boxModels.get(random.nextInt(boxModels.size())); // Random box model
        Node boxNode = (Node) assetManager.loadModel(boxModel);           // Load box model

        boxNode.setLocalScale(0.5f + random.nextFloat() * 0.5f); // Randomize size
        boxNode.setLocalTranslation(path.get(0)); // Start at the first position

        // Add box to the scene
        rootNode.attachChild(boxNode);

        // Create a MotionPath for the box
        MotionPath motionPath = new MotionPath();

        // Add all waypoints (positions) from the path list
        for (Vector3f waypoint : path) {
            motionPath.addWayPoint(waypoint);
        }

        // Create a MotionEvent to move the box along the path
        MotionEvent motionEvent = new MotionEvent(boxNode, motionPath);
        motionEvent.setSpeed(2f + random.nextFloat() * 1f); // Randomize speed
        motionEvent.setDirectionType(MotionEvent.Direction.PathAndRotation); // Rotate along path
        motionEvent.setRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y)); // Face the movement

        // Set a listener to remove the box once the animation ends
        motionPath.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent control, int wayPointIndex) {
                if (wayPointIndex == motionPath.getNbWayPoints() - 1) {
                    rootNode.detachChild(boxNode); // Remove the box once it reaches the end
                }
            }
        });

        // Start the animation
        motionEvent.play();
    }
}
