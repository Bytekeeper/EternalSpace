package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 19.10.2016.
 */
public class Physics implements Component, Pool.Poolable {
    public com.badlogic.gdx.physics.box2d.Body physicsBody;

    @Override
    public void reset() {
        physicsBody.getWorld().destroyBody(physicsBody);
        physicsBody = null;
    }
}
