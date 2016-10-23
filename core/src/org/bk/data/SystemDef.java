package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 23.10.2016.
 */
public class SystemDef {
    public String name;
    public Vector2 position;
    public Array<EntityInstance> contains;

    public static class EntityInstance {
        public String id;
        public String name;
        public Array<Component> components;
    }
}
