package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.Faction;

/**
 * Created by dante on 20.10.2016.
 */
public class Owned implements Component, Pool.Poolable {
    public Entity owner;
    public Faction affiliation;

    @Override
    public void reset() {
        owner = null;
        affiliation = null;
    }
}
