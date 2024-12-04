package hackathon2024.hackatoons.service;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FinalGame extends SimpleApplication {

    List<Vector3f> accepted = Arrays.asList(
            new Vector3f(0, 0.25f, 0),
            new Vector3f(5, 0.25f, 0),
            new Vector3f(10, 0.25f, 0),
            new Vector3f(10, 0.25f, -10)
    );

    List<Vector3f> rejected = Arrays.asList(
            new Vector3f(0, 0.25f, 0),
            new Vector3f(5, 0.25f, 0),
            new Vector3f(10, 0.25f, 0),
            new Vector3f(10, 0.25f, 10)
    );

    private BitmapText scoreText;

    public static void main(String[] args) {
        FinalGame app = new FinalGame();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Warehouse Wizardry");
        settings.setResolution(1280, 720);
        settings.setFullscreen(false);

        try {
            BufferedImage[] icons = new BufferedImage[]{
                    ImageIO.read(FinalGame.class.getResourceAsStream("/Interface/Icons/icon16.png")),
                    ImageIO.read(FinalGame.class.getResourceAsStream("/Interface/Icons/icon32.png"))
            };
            settings.setIcons(icons);
        } catch (IOException e) {
            Logger.getLogger(FinalGame.class.getName()).log(Level.SEVERE, "Failed to load icons", e);
        }

        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(0, 8, 15));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        setDisplayFps(false);
        setDisplayStatView(false);

        exitDock();
        floor();
        upBar(assetManager);
        baseBar(assetManager);
        downBar(assetManager);
        storeDock();
        addAmbientLighting();

        // Initialize and spawn boxes
        BoxManager boxManager = new BoxManager(assetManager, rootNode, accepted, rejected);
        boxManager.spawnAndAnimateBoxes(10);
    }

    public void addAmbientLighting() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(ambient);
    }
    public void downBar(AssetManager assetManager) {
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
    public void upBar(AssetManager assetManager) {
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
    private void exitDock() {
        Node conveyorBeltNode = new Node("ConveyorBelt");
        Node systemExit = (Node) assetManager.loadModel("Models/cover-window.obj");
        systemExit.rotate(0, -FastMath.HALF_PI, 0);
        systemExit.setLocalTranslation(10f, 0f, 10f);
        systemExit.scale(1f);

        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("ExitDock");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(9f, 2f, 10); // Adjust the position
        conveyorBeltNode.attachChild(title);
        conveyorBeltNode.attachChild(systemExit);
        rootNode.attachChild(conveyorBeltNode);
    }

    private void storeDock() {
        Node conveyorBeltNode = new Node("ConveyorBelt");
        Node storage = (Node) assetManager.loadModel("Models/cover-stripe-window.obj");
        storage.setLocalTranslation(10f, 0f, -10f);
        storage.rotate(0, FastMath.HALF_PI, 0);
        storage.scale(1f);

        BitmapText title = new BitmapText(guiFont, false);
        title.setSize(1f);
        title.setText("Stock Room");
        title.setColor(ColorRGBA.White);
        title.setLocalTranslation(10f, 3f, -10);
        conveyorBeltNode.attachChild(title);

        conveyorBeltNode.attachChild(storage);
        rootNode.attachChild(conveyorBeltNode);
    }

    public void baseBar(AssetManager assetManager) {
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

    private void floor() {
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
}
