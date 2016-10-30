package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 23.10.2016.
 */
public class EntityInstance {
    public String id;
    public EntityTemplate template;
    public Array<Component> components;
}
