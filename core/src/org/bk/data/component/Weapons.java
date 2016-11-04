package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import org.bk.data.EntityTemplate;

/**
 * Created by dante on 15.10.2016.
 */
public class Weapons implements Component {
    public final Array<Weapon> weapon = new Array<Weapon>();

    public static class Weapon extends Mount {
        public float cooldown;
        public float cooldownPerShot;
        public boolean firing;
        public EntityTemplate projectile;
    }
}
