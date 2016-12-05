package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;
import org.bk.PooledArray;
import org.bk.system.Box2DPhysicsSystem;

/**
 * Created by dante on 01.12.2016.
 */
public class Beam implements Component, Pool.Poolable {
    public float damagePerSecond;
    public String hitEffect;
    public Entity effectEntity;
    public final PooledArray<Box2DPhysicsSystem.TouchInfo> touched = new PooledArray(false, 4, Box2DPhysicsSystem.TouchInfo.class);

    @Override
    public void reset() {
        damagePerSecond = 0;
        hitEffect = null;
        touched.clear();
        effectEntity = null;
    }
}
