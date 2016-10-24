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
    public Vector2 position;
    public float asteroidDensity;
    public Array<EntityInstance> state;
}
