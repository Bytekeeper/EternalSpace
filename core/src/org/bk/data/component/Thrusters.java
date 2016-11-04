package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 04.11.2016.
 */
public class Thrusters implements Component, Pool.Poolable {
    public final Array<Thruster> thruster = new Array<Thruster>();

    @Override
    public void reset() {
        thruster.clear();
    }


    public static class Thruster extends Mount {
    }
}
