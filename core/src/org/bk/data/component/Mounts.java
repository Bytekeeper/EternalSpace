package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 15.10.2016.
 */
public class Mounts implements Component {
    public final Array<Weapon> weapons = new Array<Weapon>();
    public final Array<Thruster> thrusters = new Array<Thruster>();

    public static class Mount {
        public final Vector2 offset = new Vector2();
        public float orientRad;

    }

    public static class Weapon extends Mount {
        public float cooldown;
        public float cooldownPerShot;
        public boolean firing;
        public String projectileName;
    }

    public static class Thruster extends Mount {
    }
}
