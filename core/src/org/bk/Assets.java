package org.bk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by dante on 13.10.2016.
 */
public class Assets {
    private final TextureAtlas atlas;
    public final TextureRegion flare_placeholder;
    public final TextureRegion projectile_placeholder;
    public final TextureRegion ui_radar;
    public final TextureRegion asteroid_placeholder;
    public TextureRegion ship_placeholder;
    public TextureRegion planet_placeholder;
    public TextureRegion bg_star;
    public BitmapFont debugFont;

    public Assets() {
        atlas = new TextureAtlas(Gdx.files.internal("textures.atlas"));
        ship_placeholder = tr("ship/sparrow");
        bg_star = tr("particle");
        planet_placeholder = tr("planet/forest2");
        flare_placeholder = tr("effect/small+1");
        projectile_placeholder = tr("projectile/ion bolt~0");
        ui_radar = tr("ui/radar_placeholder");
        asteroid_placeholder = tr("asteroid/asteroid_placeholder");
        debugFont = new BitmapFont();
    }

    private TextureRegion tr(String name) {
        return atlas.findRegion(name);
    }
}
