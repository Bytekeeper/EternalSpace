package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 05.12.2016.
 */
public class TextureComponent implements Component, Pool.Poolable{
    public TextureRegion textureRegion;

    @Override
    public void reset() {
        textureRegion = null;
    }
}
