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
        EntityDef sourceEntity = getEntityDef(name);
        Entity entity = copyComponentIntoNewEntity(sourceEntity.components);
        return entity;
    }

    private EntityDef getEntityDef(String name) {
        EntityDef sourceEntity = (EntityDef) definitions.get(name);
        if (sourceEntity == null) {
            throw new IllegalStateException("No such entity: " + name);
        }
        return sourceEntity;
    }

    private Entity copyComponentIntoNewEntity(Array<Component> components) {

        Entity entity = engine.createEntity();
        copyComponentsIntoEntity(components, entity);
        engine.addEntity(entity);
        return entity;
    }

    private void copyComponentsIntoEntity(Array<Component> components, Entity entity) {
        for (Component component : components) {
            entity.add(kryo.copy(component));
        }
    }

    public void fabricateSystem(String name) {
        SystemDef systemDef = (SystemDef) definitions.get(name);
        if (systemDef == null) {
            throw new IllegalStateException("No such system: " + name);
        }
        kryo.getContext().put("engine", engine);
        for (SystemDef.EntityInstance entityInstance : systemDef.contains) {
            Entity entity;
            if (entityInstance.id != null) {
                entity = getOrCreateEntity(kryo, entityInstance.id);
            } else {
                entity = engine.createEntity();
            }
            copyComponentsIntoEntity(getEntityDef(entityInstance.name).components, entity);
            kryo.setDefaultSerializer(MergingSerializer.class);
            copyComponentsIntoEntity(entityInstance.components, entity);
            kryo.setDefaultSerializer(FieldSerializer.class);
            engine.addEntity(entity);
        }
        kryo.getContext().clear();
    }

    public SystemDef getSystem(String system) {
        SystemDef result = (SystemDef) definitions.get(system);
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
        protected T createCopy(Kryo kryo, T original) {
            return (original instanceof Component) ? original : super.createCopy(kryo, original);
        }

        @Override
        public T copy(Kryo kryo, T original) {
            if (original instanceof EntityRef) {
                EntityRef ref = (EntityRef) original;
                return (T) getOrCreateEntity(kryo, ref.id);
            }
            return super.copy(kryo, original);
        }
    }

    private static Entity getOrCreateEntity(Kryo kryo, String id) {
        Entity result = (Entity) kryo.getContext().get("_" + id);
        if (result == null) {
            PooledEngine engine = (PooledEngine) kryo.getContext().get("engine");
            result = engine.createEntity();
            kryo.getContext().put("_" + id, result);
        }
        return result;
    }

    public static class EntityRef extends Entity {
        public String id;
    }
}
