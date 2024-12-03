package hackathon2024.hackatoons.service.Game;

import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;

public class Light extends  Game{

    public void addAmbientLighting() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(ambient);
    }
}
