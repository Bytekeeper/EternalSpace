package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

/**
 * Created by dante on 18.10.2016.
 */
public class Landing implements Component {
    public boolean isLiftingOff;
    public float duration;
    public float timeRemaining;
    public Entity target;
}
