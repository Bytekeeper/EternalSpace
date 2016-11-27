package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import org.bk.PooledArray;

/**
 * Created by dante on 16.10.2016.
 */
public class Effects implements Component, Pool.Poolable {
    public final PooledArray<Effect> effects = new PooledArray<Effect>(false, 4, Effect.class);
    public boolean removeEntityWhenDone = false;

    @Override
    public void reset() {
        effects.clear();
        removeEntityWhenDone = false;
    }

    public static class Effect extends Mount {
        public String effect;
    }
}
