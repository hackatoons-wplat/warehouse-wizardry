package hackathon2024.hackatoons.service;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import hackathon2024.hackatoons.service.levelZero.Stations;

public class LoadingWindow extends SimpleApplication {

    private BitmapText loadingText;
    private AudioNode audioNode;

    public static void main(String[] args) {
        LoadingWindow app = new LoadingWindow();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Loading Window");
        settings.setResolution(1280, 720);
        settings.setFullscreen(false);
//        settings.setDisplayFps(false);
//        settings.setDisplayStatView(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initLoadingScreen();
        initAudio();
        setupInput();
    }

    private void initLoadingScreen() {
        guiNode.detachAllChildren();
        // Load and display an image below the text
        Picture loadingImage = new Picture("Loading Image");
        loadingImage.setImage(assetManager, "Models/vanderlande.jpeg", true);
        loadingImage.setWidth(1280);  // Set the width of the image
        loadingImage.setHeight(720); // Set the height of the image
        guiNode.attachChild(loadingImage);
        loadingText = new BitmapText(guiFont, false);
        loadingText.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        loadingText.setText("....Press Enter to be future ready....");
        loadingText.setColor(ColorRGBA.White);
        loadingText.setLocalTranslation(settings.getWidth() / 2 - loadingText.getLineWidth() / 2, settings.getHeight()-50  + loadingText.getLineHeight() / 2, 0);
        guiNode.attachChild(loadingText);
    }

    private void setupInput() {
        inputManager.addMapping("Warehouse Wizardry", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, "Warehouse Wizardry");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Warehouse Wizardry") && !isPressed) {
                Stations app = new Stations();
                AppSettings settings = new AppSettings(true);
                settings.setTitle("Warehouse Wizardry");
                settings.setResolution(1280, 720);
                settings.setFullscreen(false);
                app.setSettings(settings);
                audioNode.stop();
                app.start();
            }
        }
    };

    private void initAudio() {
        audioNode = new AudioNode(assetManager, "Sounds/sample-1.wav", AudioData.DataType.Stream);
        audioNode.setLooping(true);  // Enable looping
        audioNode.setPositional(false);  // Non-positional sound
        audioNode.setVolume(0.5f);  // Set volume
        rootNode.attachChild(audioNode);
        audioNode.play();  // Play the audio
    }

}
