package org.bk.data.component;

import com.badlogic.ashley.core.Component;

/**
 * Created by dante on 16.10.2016.
 */
public class Health implements Component {
    public float hull;
    public float shields;

    public Health reset() {
        hull = 0;
        shields = 0;
        return this;
    }
}
