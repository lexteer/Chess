package lexteer.chess.app;

import com.badlogic.gdx.Game;
import lexteer.chess.assets.Assets;

public class Main extends Game {
    public Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        assets.loadAll();
        Assets.finish();

        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }

}
