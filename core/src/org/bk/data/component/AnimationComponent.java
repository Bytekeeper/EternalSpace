package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 05.12.2016.
 */
public class AnimationComponent implements Component, Pool.Poolable {
    public float frameDuration;
    public float animationTime;
    public final Array<TextureRegion> frames = new Array<TextureRegion>();

    @Override
    public void reset() {
        frameDuration = animationTime = 0;
        frames.clear();
    }
}
