package hackathon2024.hackatoons.service;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

import javax.swing.*;
import java.util.LinkedList;
import java.util.Queue;

public class ConveyorBeltWithProgressBarAndCount extends SimpleApplication {

    private Node conveyorBeltNode = new Node("ConveyorBelt");
    private Node handNode = new Node("Hand");
    private Queue<Carrier> carriers = new LinkedList<>();
    private Geometry humanHand;
    private boolean isPickingUp = false;
    private Geometry currentCarrier;
    private float handProgress = 0f;

    private float conveyorSpeed = 2f;

    private Geometry progressBarBackground;
    private Geometry progressBarForeground;
    private float totalCarrierCount = 20f; // Total number of carriers
    private float carriersPassed = 0f; // Keep track of how many carriers have passed

    private int validCarriers = 0; // Count of valid carriers
    private int invalidCarriers = 0; // Count of invalid carriers

    private BitmapText validCarrierText;
    private BitmapText invalidCarrierText;
    private boolean isRunning = true;
    private Long decisionCount;
    private Action onRClick;
    private Action onLClick;

    public static void main(String[] args) {
        ConveyorBeltWithProgressBarAndCount app = new ConveyorBeltWithProgressBarAndCount();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initConveyorBelt();
        initCarriers();
        initHand();
        initMouseInput();
        initProgressBar();
        initTextFields();

        // Set up the camera
        cam.setLocation(new Vector3f(0, 8, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    private void initConveyorBelt() {
        Box beltShapeHorizontal = new Box(10, 0.1f, 2);
        Geometry horizontalBelt = new Geometry("HorizontalBelt", beltShapeHorizontal);
        Material beltMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beltMaterial.setColor("Color", ColorRGBA.LightGray);
        horizontalBelt.setMaterial(beltMaterial);
        horizontalBelt.setLocalTranslation(0, 0, 0);
        conveyorBeltNode.attachChild(horizontalBelt);

        rootNode.attachChild(conveyorBeltNode);
    }

    private void initCarriers() {
        for (int i = 0; i < 20; i++) {
            boolean isHeavy = (i == 4 || i == 10); // Example logic to mark some carriers as heavy
            carriers.add(createCarrier(i, isHeavy));
        }
    }

    private Carrier createCarrier(int index, boolean isHeavy) {
        Box carrierShape = new Box(0.5f, 0.5f, 0.5f);
        Geometry carrierGeometry = new Geometry("Carrier" + index, carrierShape);
        Material carrierMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        if (isHeavy) {
            carrierMaterial.setColor("Color", ColorRGBA.Red);
        } else {
            carrierMaterial.setColor("Color", ColorRGBA.Orange);
        }
        carrierGeometry.setMaterial(carrierMaterial);
        carrierGeometry.setLocalTranslation(-10 - index * 2, 0.5f, 0);
        conveyorBeltNode.attachChild(carrierGeometry);

        return new Carrier(carrierGeometry, isHeavy);
    }

    private void initHand() {
        Box handShape = new Box(1, 0.1f, 1);
        humanHand = new Geometry("Hand", handShape);
        Material handMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        handMaterial.setColor("Color", ColorRGBA.Brown);
        humanHand.setMaterial(handMaterial);
        humanHand.setLocalTranslation(0, 3, 0);
        handNode.attachChild(humanHand);
        rootNode.attachChild(handNode);
    }

    private void initProgressBar() {
        // Background for progress bar
        Quad backgroundQuad = new Quad(400, 20); // Width and height in pixels
        progressBarBackground = new Geometry("ProgressBarBackground", backgroundQuad);
        Material backgroundMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundMaterial.setColor("Color", ColorRGBA.Gray);
        progressBarBackground.setMaterial(backgroundMaterial);
        progressBarBackground.setLocalTranslation(10, settings.getHeight() - 40, 0);
        guiNode.attachChild(progressBarBackground);

        // Foreground for progress bar
        Quad foregroundQuad = new Quad(0, 20); // Initially 0 width
        progressBarForeground = new Geometry("ProgressBarForeground", foregroundQuad);
        Material foregroundMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        foregroundMaterial.setColor("Color", ColorRGBA.Green);
        progressBarForeground.setMaterial(foregroundMaterial);
        progressBarForeground.setLocalTranslation(10, settings.getHeight() - 40, 0);
        guiNode.attachChild(progressBarForeground);
    }

    private void initTextFields() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        validCarrierText = new BitmapText(font, false);
        validCarrierText.setSize(20);
        validCarrierText.setColor(ColorRGBA.Green);
        validCarrierText.setText("Valid: 0");
        validCarrierText.setLocalTranslation(settings.getWidth() - 200, 50, 0);
        guiNode.attachChild(validCarrierText);

        invalidCarrierText = new BitmapText(font, false);
        invalidCarrierText.setSize(20);
        invalidCarrierText.setColor(ColorRGBA.Red);
        invalidCarrierText.setText("Invalid: 0");
        invalidCarrierText.setLocalTranslation(settings.getWidth() - 200, 30, 0);
        guiNode.attachChild(invalidCarrierText);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!isRunning) {
            return; // Skip updates when stopped
        }

        if (isPickingUp) {
            performPickupAnimation(tpf);
            return;
        }

        // Move all carriers on the conveyor belt
        for (Carrier carrier : carriers) {
            carrier.geometry.move(conveyorSpeed * tpf, 0, 0);
        }

        // Remove carriers that move beyond the visible area
        carriers.removeIf(carrier -> {
            Vector3f position = carrier.geometry.getLocalTranslation();
            if (position.x > 15) {
                if (carrier.isHeavy) {
                    invalidCarriers++;
                } else {
                    validCarriers++;
                }
                conveyorBeltNode.detachChild(carrier.geometry);
                updateTextFields();
                return true;
            }
            return false;
        });

        //carriersNotPassed = totalCarrierCount - carriers.size();
        //float progress = carriersPassed / totalCarrierCount;
        //progressBarForeground.setMesh(new Quad(progress * 400, 20));

        
        // Update progress bar
        carriersPassed = totalCarrierCount - carriers.size();
        float progress = carriersPassed / totalCarrierCount;
        progressBarForeground.setMesh(new Quad(progress * 400, 20));
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
            isRunning = false; // Stop conveyor on right click
        }
        if (name.equals("ResumeConveyor") && !isPressed) {
            isRunning = true; // Resume conveyor on left click
        }
    };

    private void performPickupAnimation(float tpf) {
        if (handProgress < 1) {
            humanHand.move(0, -tpf * 3, 0);
            handProgress += tpf;
        } else if (handProgress < 2) {
            currentCarrier.move(0, tpf * 3, 0);
            humanHand.move(0, tpf * 3, 0);
            handProgress += tpf;
        } else {
            conveyorBeltNode.detachChild(currentCarrier);
            carriers.poll();
            currentCarrier = null;
            isPickingUp = false;
            handProgress = 0;
            humanHand.setLocalTranslation(0, 3, 0);
        }
    }

    private void updateTextFields() {
        validCarrierText.setText("Valid: " + validCarriers); //needs more validation
        invalidCarrierText.setText("Invalid: " + invalidCarriers);
    }

    private static class Carrier {
        Geometry geometry;
        boolean isHeavy;

        Carrier(Geometry geometry, boolean isHeavy) {
            this.geometry = geometry;
            this.isHeavy = isHeavy;
        }
    }
}
