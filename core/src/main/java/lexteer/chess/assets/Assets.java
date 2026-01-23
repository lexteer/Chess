package lexteer.chess.assets;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;

public class Assets {

    private static final AssetManager manager = new AssetManager();

    AssetLoader loader;

    public Assets() {
        loader = new AssetLoader();
    }

    public void loadAll() {
        loader.load();
    }

    public static <T> void load(String path, Class<T> type) {
        if(!manager.isLoaded(path)) {
            manager.load(path, type);
        }
    }

    public static <T> void load(String path, Class<T> type, AssetLoaderParameters<T> params) {
        if (!manager.isLoaded(path)) {
            manager.load(path, type, params);
        }
    }

    public static <T> T get(String path, Class<T> type) {
        return manager.get(path, type);
    }

    // ---- update / finish ----
    public static boolean update() {
        return manager.update();
    }

    public static void finish() {
        manager.finishLoading();
    }

    public static float progress() {
        return manager.getProgress();
    }

    public void dispose() {
        manager.dispose();
    }
}
