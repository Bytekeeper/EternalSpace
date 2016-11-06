package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import org.bk.script.Initializable;
import org.bk.Player;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

/**
 * Created by dante on 22.10.2016.
 */
public class GameData implements Initializable {
    private PooledEngine engine;
    public ObjectMap<String, SolarSystem> system = new ObjectMap<String, SolarSystem>();
    public ObjectMap<String, EntityTemplate> template = new ObjectMap<String, EntityTemplate>();
    public Array<JumpLink> link = new Array<JumpLink>();
    public ObjectMap<String, GameEvent> event = new ObjectMap<String, GameEvent>();
    public ObjectMap<String, Mission> mission = new ObjectMap<String, Mission>();
    public Array<Mission> activeMission = new Array<Mission>();
    public ObjectMap<String, Faction> faction = new ObjectMap<String, Faction>();
    public ObjectMap<String, EntityGroup> group = new ObjectMap<String, EntityGroup>();
    public Entity player;

    public GameData() {
    }

    public void setEngine(PooledEngine engine) {
        this.engine = engine;
    }


    public Array<SolarSystem> getSystem() {
        return system.values().toArray();
    }

    @Override
    public void afterFieldsSet() {
        for (JumpLink l : link) {
            l.a.links.add(l);
            l.b.links.add(l);
        }
        for (EntityGroup g: group.values()) {
            g.afterFieldsSet();
        }
    }
}
