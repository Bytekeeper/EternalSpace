package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import org.bk.PooledArray;
import org.bk.data.EntityTemplate;

/**
 * Created by dante on 15.10.2016.
 */
public class Weapons implements Component, Pool.Poolable {
    public final PooledArray<Weapon> weapon = new PooledArray<Weapon>(false, 4, Weapon.class);

    @Override
    public void reset() {
        weapon.clear();
    }

    public static class Weapon extends Mount {
        public float cooldown;
        public float cooldownPerShot;
        public boolean firing;
        public String muzzleEffect;
        public EntityTemplate projectile;
    }
}
