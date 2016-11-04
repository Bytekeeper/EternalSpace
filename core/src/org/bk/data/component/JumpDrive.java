package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 04.11.2016.
 */
public class JumpDrive implements Component, Pool.Poolable {
    public float powerCost;

    @Override
    public void reset() {
        powerCost = 0;
    }
}
