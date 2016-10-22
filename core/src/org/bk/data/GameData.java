package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

/**
 * Created by dante on 22.10.2016.
 */
public class GameData {
    public Array<String> imports;
    private Kryo kryo = new Kryo();
    private PooledEngine engine;
    private ObjectMap<String, Object> definitions = new ObjectMap<String, Object>();

    public GameData() {
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

    public void addAll(ObjectMap<String, Object> definitions) {
        this.definitions.putAll(definitions);
    }

    public Entity fabricateEntity(String name) {
        Entity entity = engine.createEntity();
        EntityDef sourceEntity = (EntityDef) definitions.get(name);
        if (sourceEntity == null) {
            throw new IllegalStateException("No such entity: " + name);
        }
        for (Component component: sourceEntity.components) {
            entity.add(kryo.copy(component));
        }
        return entity;
    }
}
