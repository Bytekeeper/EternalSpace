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
    public Array<String> imports;
    private Kryo kryo = new Kryo();
    private PooledEngine engine;
    public ObjectMap<String, SolarSystem> system = new ObjectMap<String, SolarSystem>();
    public ObjectMap<String, EntityTemplate> template = new ObjectMap<String, EntityTemplate>();
    private ObjectMap<String, Faction> factions = new ObjectMap<String, Faction>();
    private Array<SolarSystem> systemArray = new Array<SolarSystem>(false, 10);
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
        kryo.setDefaultSerializer(MergingSerializer.class);
    }

    public void setEngine(PooledEngine engine) {
        this.engine = engine;
    }

    public void addAll(ObjectMap<String, Object> definitions) {
        for (ObjectMap.Entry<String, Object> entry : definitions.entries()) {
            Object value = entry.value;
            if (value instanceof SolarSystem) {
                SolarSystem solarSystem = (SolarSystem) value;
                solarSystem.name = entry.key;
                system.put(entry.key, solarSystem);
                systemArray.add(solarSystem);
            } else if (value instanceof EntityTemplate) {
                template.put(entry.key, (EntityTemplate) value);
            } else if (value instanceof JumpLink) {
                jumpLinks.add((JumpLink) value);
            }
        }
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
        for (EntityInstance entityInstance : solarSystem.entity) {
            Entity entity;
            if (entityInstance.id != null) {
                entity = getOrCreateEntity(kryo, entityInstance.id);
            } else {
                entity = engine.createEntity();
            }
            entityInstance.template.applyTo(kryo, entity, entityInstance.components);
            engine.addEntity(entity);
        }
    }

    public SolarSystem getSystem(String system) {
        return this.system.get(system);
    }

    public Array<SolarSystem> getSystem() {
        return systemArray;
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
                return (T) gameData(kryo).factions.get(((FactionRef) original).id);
            } else if (original instanceof SolarSystemRef) {
                return (T) gameData(kryo).system.get(((SolarSystemRef) original).getId());
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

    public static class SolarSystemRef extends SolarSystem implements Reference {
        private String id;

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
