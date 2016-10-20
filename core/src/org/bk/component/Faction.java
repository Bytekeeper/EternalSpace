package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 20.10.2016.
 */
public class Faction implements Component {
    public int index;
    public Array<Float> assessment = new Array<Float>();
}
