package org.bk.data;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;

/**
 * Created by dante on 23.10.2016.
 */
public class SolarSystem {
    public String name;
    public final Vector2 position = new Vector2();
    public float asteroidDensity;
    public Array<EntityInstance> entity = new Array<EntityInstance>();
    public Array<JumpLink> links = new Array<JumpLink>();
}
