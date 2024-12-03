package hackathon2024.hackatoons.service.Game;

import com.jme3.app.SimpleApplication;
import hackathon2024.hackatoons.service.Game.Conveyor.*;
import hackathon2024.hackatoons.service.Game.Light;
public class Game  extends SimpleApplication {

    public static void main(String args[]) {
        Game app = new Game();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Conveyor conveyor = new Conveyor();
        Light light = new Light();
        light.addAmbientLighting();
        conveyor.initConveyorUp(assetManager);
        conveyor.initConveyorBarsDown(assetManager);
        conveyor.initConveyorBase(assetManager);
    }
}
