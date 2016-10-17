package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 10.10.2016.
 */
public class Movement implements Component {
    public final Vector2 velocity = new Vector2();
    public final Vector2 linearAccel = new Vector2();
    public float angularVelocity;
    public float angularAccel;
    public float maxVelocity;

    public float linearThrust;
    public float angularThrust;
}
