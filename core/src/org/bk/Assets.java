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
    public TextureRegion ship_placeholder;
    public TextureRegion planet_placeholder;
    public TextureRegion bg_star;
    public BitmapFont debugFont;

    public Assets() {
        atlas = new TextureAtlas(Gdx.files.internal("textures.atlas"));
        ship_placeholder = tr("ship/sparrow");
        bg_star = tr("particle");
        planet_placeholder = tr("planet/forest2");
        debugFont = new BitmapFont();
    }

    private TextureRegion tr(String name) {
        return atlas.findRegion(name);
    }
}
