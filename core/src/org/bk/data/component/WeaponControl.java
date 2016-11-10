package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Created by dante on 10.11.2016.
 */
public class WeaponControl implements Component, Poolable {
    public boolean primaryFire;

    @Override
    public void reset() {
        primaryFire = false;
    }
}
