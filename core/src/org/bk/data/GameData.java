package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializerConfig;
import org.bk.script.Initializable;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

/**
 * Created by dante on 22.10.2016.
 */
public class GameData implements Initializable {
    private Kryo kryo = new Kryo();
    private PooledEngine engine;
    public ObjectMap<String, SolarSystem> system = new ObjectMap<String, SolarSystem>();
    public ObjectMap<String, EntityTemplate> template = new ObjectMap<String, EntityTemplate>();
    public Array<JumpLink> link = new Array<JumpLink>();
    public ObjectMap<String, GameEvent> event = new ObjectMap<String, GameEvent>();
    public ObjectMap<String, Mission> mission = new ObjectMap<String, Mission>();
    public Array<Mission> activeMission = new Array<Mission>();
    public ObjectMap<String, Faction> faction = new ObjectMap<String, Faction>();
    public Entity player;


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
        return spawnEntity(template);
    }

    public Entity spawnEntity(EntityTemplate template) {
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

    public void spawnSystem(SolarSystem solarSystem) {
        for (Entity entity : solarSystem.entity) {
            engine.addEntity(entity);
        }
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
    }
}
