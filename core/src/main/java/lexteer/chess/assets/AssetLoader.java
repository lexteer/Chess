package lexteer.chess.assets;

import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;

public class AssetLoader {
    public AssetLoader() {
        load();
    }
    public void load() {
        TextureLoader.TextureParameter p = new TextureLoader.TextureParameter();
        p.genMipMaps = true;
        p.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        p.magFilter = Texture.TextureFilter.Linear;

        Assets.load("pieces/white/pawn.png", Texture.class, p);
        Assets.load("pieces/white/knight.png", Texture.class, p);
        Assets.load("pieces/white/bishop.png", Texture.class, p);
        Assets.load("pieces/white/rook.png", Texture.class, p);
        Assets.load("pieces/white/queen.png", Texture.class, p);
        Assets.load("pieces/white/king.png", Texture.class, p);

        Assets.load("pieces/black/pawn.png", Texture.class, p);
        Assets.load("pieces/black/knight.png", Texture.class, p);
        Assets.load("pieces/black/bishop.png", Texture.class, p);
        Assets.load("pieces/black/rook.png", Texture.class, p);
        Assets.load("pieces/black/queen.png", Texture.class, p);
        Assets.load("pieces/black/king.png", Texture.class, p);
    }
}
