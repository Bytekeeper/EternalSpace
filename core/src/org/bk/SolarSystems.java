package org.bk;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 20.10.2016.
 */
public class SolarSystems {
    private ObjectMap<String, SolarSystem> systems = new ObjectMap<String, SolarSystem>();
    private final Game game;

    public SolarSystems(Game game) {
        this.game = game;
    }

    public float orientationToward(String target) {
        return MathUtils.PI / 3;
    }

    public float orientationFrom(String sourceOrTargetSystem) {
        return (orientationToward(sourceOrTargetSystem) + MathUtils.PI) % MathUtils.PI2;
    }

    public void populateCurrentSystem() {
        if ("initial".equals(game.currentSystem)) {
            game.spawn("planet");
            TRANSFORM.get(game.spawn("planet")).location.set(2000, 1000);
        } else {
            for (int i = 0; i < 10; i++) {
                TRANSFORM.get(game.spawn("planet")).location.setToRandomDirection().scl(i * 300);
            }
        }
    }

    public static class SolarSystem {
        public final Vector2 galacticPosition = new Vector2();
        public final Array<Link> links = new Array<Link>();
    }

    private static class Link {
        public SolarSystem a;
        public SolarSystem b;
    }
}
