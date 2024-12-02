package hackathon2024.hackatoons.service.levelZero;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.shape.Box;

    public class Storage extends SimpleApplication {


        private Node storageNode = new Node("Storage");
        private boolean isPaused = false;
        private boolean isBoxMoved = false;

        public static void main(String[] args) {
            Storage app = new Storage();
            app.start();
        }

        @Override
        public void simpleInitApp() {
            initStorage();
            setupInput();
        }

        private void initStorage() {
            // Create a simple box to represent storage
            Box storageShape = new Box(1, 1, 1);
            Geometry storageGeometry = new Geometry("Storage", storageShape);
            Material storageMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            storageMaterial.setColor("Color", ColorRGBA.Green);
            storageGeometry.setMaterial(storageMaterial);
            storageGeometry.setLocalTranslation(0, 0, 0);
            storageNode.attachChild(storageGeometry);
            rootNode.attachChild(storageNode);
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
                if (!isBoxMoved) {
                    if (name.equals("LeftClick") && !isPressed) {
                        moveUpward();
                        isBoxMoved = true;
                    } else if (name.equals("RightClick") && !isPressed) {
                        moveDownward();
                        isBoxMoved = true;
                    }
                }
                if (name.equals("Pause") && !isPressed) {
                    isPaused = !isPaused;
                }
            }
        };

        private void moveUpward() {
            if (!isPaused) {
                // Define the action to move upward
                Vector3f currentLocation = cam.getLocation();
                cam.setLocation(currentLocation.add(0, 5, 0));
            }
        }

        private void moveDownward() {
            if (!isPaused) {
                // Define the action to move downward
                Vector3f currentLocation = cam.getLocation();
                cam.setLocation(currentLocation.add(0, -5, 0));
            }
        }
        }







