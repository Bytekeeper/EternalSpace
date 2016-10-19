package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import org.bk.EntityFactory;

/**
 * Created by dante on 15.10.2016.
 */
public class Mounts implements Component {
    public final Array<Weapon> weapons = new Array<Weapon>();
    public final Array<Vector2> thrusters = new Array<Vector2>();

    public static class Weapon {
        public final Vector2 offset = new Vector2();
        public float cooldown;
        public float cooldownPerShot;
        public boolean firing;
        public float orientRad;
        public EntityFactory.EntityDefinitionKey projectileDefinition;
    }
}
