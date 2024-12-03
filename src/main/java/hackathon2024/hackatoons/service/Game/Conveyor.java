package hackathon2024.hackatoons.service.Game;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.light.AmbientLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;

import java.util.Arrays;
import java.util.List;
public class Conveyor extends Game {

    public void initConveyorUp(AssetManager assetManager) {
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

    public void initConveyorBarsDown(AssetManager assetManager) {
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
    public void initConveyorBase(AssetManager assetManager) {
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
}
