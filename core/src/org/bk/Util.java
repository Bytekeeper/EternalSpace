package org.bk;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.PI2;

/**
 * Created by dante on 24.10.2016.
 */
public class Util {
    public static float deltaAngle(float fromRadians, float toRadians) {
        return ((toRadians - fromRadians + 5 *  PI) % PI2) - PI;
    }
}
