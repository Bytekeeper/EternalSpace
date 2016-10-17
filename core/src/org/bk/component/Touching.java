package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 16.10.2016.
 */
public class Touching implements Component {
    public final Array<Entity> touchList = new Array<Entity>(false, 1);
}
