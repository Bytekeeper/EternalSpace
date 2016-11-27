package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import org.bk.PooledArray;

/**
 * Created by dante on 04.11.2016.
 */
public class Thrusters implements Component, Pool.Poolable {
    public final PooledArray<Thruster> thruster = new PooledArray<Thruster>(false, 4, Thruster.class);

    @Override
    public void reset() {
        thruster.clear();
    }


    public static class Thruster extends Mount {
        public String thrusterEffect;
    }
}
