package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 03.11.2016.
 */
public class Modules implements Component, Pool.Poolable {
    public JumpDrive jumpDrive;

    @Override
    public void reset() {
        jumpDrive = null;
    }

    public static class JumpDrive {
        public float powerCost;
    }
}
