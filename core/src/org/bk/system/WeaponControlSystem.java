package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.WeaponControl;
import org.bk.data.component.Weapons;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 10.11.2016.
 */
public class WeaponControlSystem extends IteratingSystem {
    public WeaponControlSystem() {
        super(Family.all(WeaponControl.class, Weapons.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        WeaponControl weaponControl = WEAPON_CONTROL.get(entity);
        Weapons weapons = WEAPONS.get(entity);
        for (Weapons.Weapon weapon : weapons.weapon) {
            weapon.firing = weaponControl.primaryFire;
        }
    }
}
