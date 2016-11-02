package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializerConfig;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

/**
 * Created by dante on 22.10.2016.
 */
public class GameData {
    private Kryo kryo = new Kryo();
    private PooledEngine engine;
    public ObjectMap<String, SolarSystem> system = new ObjectMap<String, SolarSystem>();
    public ObjectMap<String, EntityTemplate> template = new ObjectMap<String, EntityTemplate>();
    private ObjectMap<String, Faction> factions = new ObjectMap<String, Faction>();
    private Array<JumpLink> jumpLinks = new Array<JumpLink>();

    public GameData() {
        kryo.getContext().put("gameData", this);
        kryo.setInstantiatorStrategy(new InstantiatorStrategy() {
            private InstantiatorStrategy fallback = new Kryo.DefaultInstantiatorStrategy();

            @Override
            public <T> ObjectInstantiator<T> newInstantiatorOf(final Class<T> type) {
                if (Component.class.isAssignableFrom(type)) {
                    return (ObjectInstantiator<T>) new ObjectInstantiator<Component>() {
                        @Override
                        public Component newInstance() {
                            return engine.createComponent((Class<Component>) type);
                        }
                    };
                }
                return fallback.newInstantiatorOf(type);
            }
        });
    }

    public void setEngine(PooledEngine engine) {
        this.engine = engine;
    }

    public Entity spawnEntity(String name) {
        EntityTemplate template = getEntityTemplate(name);
        Entity entity = engine.createEntity();
        template.applyTo(kryo, entity, null);
        engine.addEntity(entity);
        return entity;
    }

    private EntityTemplate getEntityTemplate(String name) {
        EntityTemplate sourceEntity = template.get(name);
        if (sourceEntity == null) {
            throw new IllegalStateException("No such entity: " + name);
        }
        return sourceEntity;
    }

    public void spawnSystem(String name) {
        SolarSystem solarSystem = system.get(name);
        if (solarSystem == null) {
            throw new IllegalStateException("No such system: " + name);
        }
        for (Entity entity: solarSystem.entity) {
            engine.addEntity(entity);
        }
    }

    public SolarSystem getSystem(String system) {
        return this.system.get(system);
    }

    public Array<SolarSystem> getSystem() {
        return system.values().toArray();
    }

    private static Entity getOrCreateEntity(Kryo kryo, String id) {
        Entity result = (Entity) kryo.getContext().get("_" + id);
        if (result == null) {
            PooledEngine engine = getEngine(kryo);
            result = engine.createEntity();
            kryo.getContext().put("_" + id, result);
        }
        return result;
    }

    private static PooledEngine getEngine(Kryo kryo) {
        return gameData(kryo).engine;
    }

    private static GameData gameData(Kryo kryo) {
        return (GameData) kryo.getContext().get("gameData");
    }
}
