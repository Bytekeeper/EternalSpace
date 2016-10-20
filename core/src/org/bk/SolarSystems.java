package org.bk;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by dante on 20.10.2016.
 */
public class SolarSystems {
    private ObjectMap<String, SystemKey> keys = new ObjectMap<String, SystemKey>();

    public SystemKey key(String name) {
        SystemKey result = keys.get(name);
        if (result != null) {
            return result;
        }
        result = new SystemKey(name);
        keys.put(name, result);
        return result;
    }

    public float orientationToward(SystemKey target) {
        return MathUtils.PI / 3;
    }

    public float orientationFrom(SystemKey sourceOrTargetSystem) {
        return (orientationToward(sourceOrTargetSystem) + MathUtils.PI) % MathUtils.PI2;
    }

    public static class SystemKey {
        public final String name;

        SystemKey(String name) {
            this.name = name;
        }
    }

}
