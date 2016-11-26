package org.bk.data.component;

import com.badlogic.ashley.core.Component;

/**
 * Created by dante on 15.10.2016.
 */
public class Projectile implements Component {
    public float yield;
    public float initialSpeed;
    public String hitEffect;

    public Projectile reset() {
        yield = 0;
        initialSpeed = 0;
        return this;
    }
}
