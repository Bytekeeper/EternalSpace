package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.*;

/**
 * Created by dante on 19.10.2016.
 */
public class Character implements Component, Pool.Poolable {
    public Faction faction;

    @Override
    public void reset() {
        faction = null;
    }
}
