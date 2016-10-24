package org.bk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import org.bk.data.GameData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dante on 13.10.2016.
 */
public class Assets {
    private final TextureAtlas atlas;
    public final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    public final I18NBundle localization;
    public final Sound snd_thrust;
    public final Sound snd_hyperdrive_engage;
    public final GameData gameData;
    public final BitmapFont hudFont;
    public final Sound snd_hyperdrive_shutdown;
    public BitmapFont debugFont;
    private final AssetManager assetManager;
    public ObjectMap<String, TextureRegion> textures = new ObjectMap<String, TextureRegion>();

    public Assets() {
        assetManager = new AssetManager();
        assetManager.setLoader(GameData.class, new JSONLoader<GameData>(GameData.class, new InternalFileHandleResolver()));
        assetManager.setLoader(ObjectMap.class, new JSONLoader<ObjectMap>(ObjectMap.class, new InternalFileHandleResolver()));
        AssetDescriptor<Sound> thrustAsset = loadSound("sound/ship/thrust.ogg");
        AssetDescriptor<Sound> hyperDriveEngage = loadSound("sound/ship/hyperdrive_engage.ogg");
        AssetDescriptor<Sound> hyperDriveShutdown = loadSound("sound/ship/hyperdrive_shutdown.ogg");
        AssetDescriptor<GameData> gameData = new AssetDescriptor<GameData>("gamedata/gamedata.json", GameData.class);
        assetManager.load(gameData);
        AssetDescriptor<TextureAtlas> atlasAssetDescriptor = new AssetDescriptor<TextureAtlas>("textures.atlas", TextureAtlas.class);
        assetManager.load(atlasAssetDescriptor);
        assetManager.finishLoading();


        atlas = assetManager.get(atlasAssetDescriptor);
        for (TextureAtlas.AtlasRegion region: atlas.getRegions()) {
            textures.put(region.name, region);
        }
        debugFont = new BitmapFont();
        localization = I18NBundle.createBundle(Gdx.files.internal("messages"));
        snd_thrust = assetManager.get(thrustAsset);
        snd_hyperdrive_engage = assetManager.get(hyperDriveEngage);
        snd_hyperdrive_shutdown = assetManager.get(hyperDriveShutdown);
        this.gameData = assetManager.get(gameData);
        for (String toLoad: this.gameData.imports) {
            assetManager.load("gamedata/" + toLoad + ".json", ObjectMap.class);
        }
        assetManager.finishLoading();
        for (String toLoad: this.gameData.imports) {
            ObjectMap<String, Object> definitions = assetManager.get("gamedata/" + toLoad + ".json", ObjectMap.class);
            this.gameData.addAll(definitions);
        }
        hudFont = skin.getFont("default-font");
    }

    private AssetDescriptor<Sound> loadSound(String fileName) {
        AssetDescriptor<Sound> hyperDriveEngage = new AssetDescriptor<Sound>(fileName, Sound.class);
        assetManager.load(hyperDriveEngage);
        return hyperDriveEngage;
    }

    private TextureRegion tr(String name) {
        return atlas.findRegion(name);
    }
}
