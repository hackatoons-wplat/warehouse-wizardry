package hackathon2024.hackatoons.service;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.input.MouseInput;
import com.jme3.input.InputManager;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.collision.*;
import com.jme3.math.*;
import com.jme3.light.AmbientLight;

public class TestCollision extends SimpleApplication {

    private Node boxNode;
    private Spatial box;

    public static void main(String[] args) {
        TestCollision app = new TestCollision();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Load the box model from the resources
        box = assetManager.loadModel("Models/box-small.obj");

        // Create a node to hold the box
        boxNode = new Node("Box Node");
        boxNode.attachChild(box);
        rootNode.attachChild(boxNode);

        // Position the box at (0,0,0)
        box.setLocalTranslation(Vector3f.ZERO);

        // Register a click listener to the box
        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "click");

        // Set up the camera
        flyCam.setMoveSpeed(10);
        flyCam.setEnabled(false);
        // Add Ambient Light to the scene
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.5f)); // Set ambient light intensity (adjust as needed)
        rootNode.addLight(ambientLight);
    }

    // ActionListener to handle clicks on the box
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed && name.equals("click")) {
                if (isMouseOverBox()) {
                    System.out.println("I clicked");
                    detachAndRespawn();
                }
            }
        }
    };

    // Check if the mouse is over the box using a raycast
    private boolean isMouseOverBox() {
        Vector2f cursorPosition = inputManager.getCursorPosition();

        // Convert the 2D cursor position to a 3D ray in the world
        Vector3f worldPosition = getCamera().getWorldCoordinates(cursorPosition, 0f);
        Vector3f direction = getCamera().getWorldCoordinates(cursorPosition, 1f).subtract(worldPosition).normalize();

        Ray ray = new Ray(worldPosition, direction);
        CollisionResults results = new CollisionResults();

        // Perform the pick (collision test) using the ray
        rootNode.collideWith(ray, results);

        return results.size() > 0; // If there was a collision
    }

    // Detach the box, and respawn it after 2 seconds
    private void detachAndRespawn() {
        boxNode.detachChild(box);
        // Wait for 2 seconds before spawning again
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Sleep for 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spawnBox();
        }).start();
    }

    // Spawn the box again
    private void spawnBox() {
        boxNode.attachChild(box);
        box.setLocalTranslation(Vector3f.ZERO);  // Spawn the box at 0,0,0
    }
}
