package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 15.10.2016.
 */
public class Projectile implements Component {
    public Entity owner;
    public float yield;
    public float initialSpeed;

    public Projectile reset() {
        owner = null;
        yield = 0;
        initialSpeed = 0;
        return this;
    }
}
