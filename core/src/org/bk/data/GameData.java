package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializerConfig;
import com.sun.webkit.graphics.Ref;
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
        kryo.setDefaultSerializer(MergingSerializer.class);
    }

    public void setEngine(PooledEngine engine) {
        this.engine = engine;
    }

    public void addAll(ObjectMap<String, Object> definitions) {
        this.definitions.putAll(definitions);
    }

    public Entity spawnEntity(String name) {
        EntityTemplate template = getEntityTemplate(name);
        Entity entity = engine.createEntity();
        template.applyTo(kryo, entity, null);
        engine.addEntity(entity);
        return entity;
    }

    private EntityTemplate getEntityTemplate(String name) {
        EntityTemplate sourceEntity = (EntityTemplate) definitions.get(name);
        if (sourceEntity == null) {
            throw new IllegalStateException("No such entity: " + name);
        }
        return sourceEntity;
    }

    public void spawnSystem(String name) {
        SolarSystem solarSystem = (SolarSystem) definitions.get(name);
        if (solarSystem == null) {
            throw new IllegalStateException("No such system: " + name);
        }
        kryo.getContext().put("gameData", this);
        for (EntityInstance entityInstance : solarSystem.state) {
            Entity entity;
            if (entityInstance.id != null) {
                entity = getOrCreateEntity(kryo, entityInstance.id);
            } else {
                entity = engine.createEntity();
            }
            getEntityTemplate(entityInstance.name).applyTo(kryo, entity, entityInstance.components);
            engine.addEntity(entity);
        }
        kryo.getContext().clear();
    }

    public SolarSystem getSystem(String system) {
        SolarSystem result = (SolarSystem) definitions.get(system);
        result.name = system;
        return result;
    }

    public static class MergingSerializer<T> extends FieldSerializer<T> {
        public MergingSerializer(Kryo kryo, Class type) {
            super(kryo, type);
        }

        public MergingSerializer(Kryo kryo, Class type, Class[] generics) {
            super(kryo, type, generics);
        }

        protected MergingSerializer(Kryo kryo, Class type, Class[] generics, FieldSerializerConfig config) {
            super(kryo, type, generics, config);
        }

        @Override
        public T copy(Kryo kryo, T original) {
            if (original instanceof EntityRef) {
                EntityRef ref = (EntityRef) original;
                return (T) getOrCreateEntity(kryo, ref.id);
            } else if (original instanceof FactionRef) {
                return (T) gameData(kryo).definitions.get(((FactionRef) original).id);
            }
            return super.copy(kryo, original);
        }
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

    public interface Reference {
        String getId();
        void setId(String id);
    }

    public static class EntityRef extends Entity implements Reference {
        public String id;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }

    public static class FactionRef extends Faction implements Reference {
        public String id;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }
}
