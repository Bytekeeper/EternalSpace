package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import org.bk.system.Box2DPhysicsSystem;

/**
 * Created by dante on 16.10.2016.
 */
public class Touching implements Component, Pool.Poolable {
    public final Array<Box2DPhysicsSystem.TouchInfo> touchList = new Array<Box2DPhysicsSystem.TouchInfo>(false, 1);
    public final Array<Entity> untouchList = new Array<Entity>(false, 1);

    @Override
    public void reset() {
        touchList.clear();
        untouchList.clear();
    }
}
