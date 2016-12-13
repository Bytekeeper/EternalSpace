package org.bk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import org.bk.data.GameData;
import org.bk.script.ScriptContext;

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
    public final ScriptContext scriptContext;
    public final Sound snd_shot1;
    public final Sound snd_engine_noise;
    public final Sound snd_beam1;
    public BitmapFont debugFont;
    private final AssetManager assetManager;
    public ObjectMap<String, TextureRegion> textures = new ObjectMap<String, TextureRegion>();
    private ObjectMap<String, Array<float[]>> outline = new ObjectMap<String, Array<float[]>>();
    public Outliner outliner = new Outliner();
    public ObjectMap<String, ParticleEffectPool> effects = new ObjectMap<String, ParticleEffectPool>();

    public Assets() {
        assetManager = new AssetManager();
        AssetDescriptor<Sound> thrustAsset = loadSound("sound/ship/thrust.ogg");
        AssetDescriptor<Sound> hyperDriveEngage = loadSound("sound/ship/hyperdrive_engage.ogg");
        AssetDescriptor<Sound> hyperDriveShutdown = loadSound("sound/ship/hyperdrive_shutdown.ogg");
        AssetDescriptor<Sound> shot1 = loadSound("sound/ship/shot1.ogg");
        AssetDescriptor<Sound> engine_noise = loadSound("sound/ship/engine_noise.ogg");
        AssetDescriptor<Sound> beam1 = loadSound("sound/ship/beam1.ogg");
        AssetDescriptor<TextureAtlas> atlasAssetDescriptor = new AssetDescriptor<TextureAtlas>("textures.atlas", TextureAtlas.class);
        assetManager.load(atlasAssetDescriptor);
        assetManager.finishLoading();


        atlas = assetManager.get(atlasAssetDescriptor);
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            textures.put(region.name, region);
        }
        debugFont = new BitmapFont();
        localization = I18NBundle.createBundle(Gdx.files.internal("messages"));
        snd_thrust = assetManager.get(thrustAsset);
        snd_hyperdrive_engage = assetManager.get(hyperDriveEngage);
        snd_hyperdrive_shutdown = assetManager.get(hyperDriveShutdown);
        snd_shot1 = assetManager.get(shot1);
        snd_engine_noise = assetManager.get(engine_noise);
        snd_beam1 = assetManager.get(beam1);
        this.gameData = new GameData();
        hudFont = skin.getFont("default-font");

        scriptContext = new ScriptContext(this.gameData);
        scriptContext.load(Gdx.files.internal("gamedata/system.def").reader());

        loadEffect("muzzle1");
        loadEffect("thrust1");
        loadEffect("slug_spark");
        loadEffect("beam1_spark");
        loadEffect("expl_medium1");
        loadEffect("expl_medium2");
        loadEffect("expl_large1");
    }

    private void loadEffect(String effectName) {
        final ParticleEffect particleEffect = new ParticleEffect();
        particleEffect.load(Gdx.files.internal("fx/" + effectName), atlas);
        effects.put(effectName, new ParticleEffectPool(particleEffect, 0, 20) {
            @Override
            protected PooledEffect newObject() {
                PooledEffect result = super.newObject();
                replaceAnglesOf(result.getEmitters());
                return result;
            }

            private void replaceAnglesOf(Array<ParticleEmitter> emitters) {
                for (int i = 0; i < emitters.size; i++) {
                    final ParticleEmitter emitter = emitters.get(i);
                    emitters.set(i, new ParticleEmitter(emitter) {
                        BiasedScaledNumericValueDelegate replacement = new BiasedScaledNumericValueDelegate(super.getAngle());
                        @Override
                        public ScaledNumericValue getAngle() {
                            return replacement;
                        }
                    });
                }
            }

            @Override
            protected void reset(PooledEffect effect) {
                super.reset(effect);
                for (int i = 0; i < effect.getEmitters().size; i++) {
                    effect.getEmitters().get(i).getAngle().load(particleEffect.getEmitters().get(i).getAngle());
                }
            }
        });
    }

    private AssetDescriptor<Sound> loadSound(String fileName) {
        AssetDescriptor<Sound> hyperDriveEngage = new AssetDescriptor<Sound>(fileName, Sound.class);
        assetManager.load(hyperDriveEngage);
        return hyperDriveEngage;
    }

    private TextureRegion tr(String name) {
        return atlas.findRegion(name);
    }

    public Array<float[]> outlineOf(String name) {
        Array<float[]> result = outline.get(name);
        if (result == null) {
            result = outliner.determineOutlineOf(textures.get(name));
            outline.put(name, result);
        }
        return result;
    }
}
