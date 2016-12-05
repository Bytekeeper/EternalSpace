package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.utils.Pool;
import org.bk.PooledArray;

/**
 * Created by dante on 16.10.2016.
 */
public class Effect implements Component, Pool.Poolable {
    public String effect;
    public ParticleEffectPool.PooledEffect particleEffect;
    public boolean removeEntityWhenDone;

    @Override
    public void reset() {
        effect = null;
        particleEffect = null;
        removeEntityWhenDone = false;
    }
}
