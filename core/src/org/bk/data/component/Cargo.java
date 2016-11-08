package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.CargoItem;

/**
 * Created by dante on 08.11.2016.
 */
public class Cargo implements Component, Pool.Poolable {
    // Items in cargo are not usable (ie. weapons in here can't be used to fire)
    public final Array<CargoItem> cargo = new Array<CargoItem>();

    @Override
    public void reset() {
        cargo.clear();
    }
}
