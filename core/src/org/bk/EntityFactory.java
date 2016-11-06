package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import org.bk.data.EntityGroup;
import org.bk.data.EntityTemplate;
import org.bk.data.GameData;
import org.bk.data.SolarSystem;
import org.bk.data.component.Character;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

/**
 * Created by dante on 06.11.2016.
 */
public class EntityFactory {
    private final GameData gameData;
    private final PooledEngine engine;
    private Kryo kryo = new Kryo();
    private Array<Entity> tEntities = new Array<Entity>();


    public EntityFactory(final Game game) {
        gameData = game.gameData;
        engine = game.engine;
        kryo.setInstantiatorStrategy(new InstantiatorStrategy() {
            private InstantiatorStrategy fallback = new Kryo.DefaultInstantiatorStrategy();

            @Override
            public <T> ObjectInstantiator<T> newInstantiatorOf(final Class<T> type) {
                if (Component.class.isAssignableFrom(type)) {
                    return (ObjectInstantiator<T>) new ObjectInstantiator<Component>() {
                        @Override
                        public Component newInstance() {
                            return game.engine.createComponent((Class<Component>) type);
                        }
                    };
                }
                return fallback.newInstantiatorOf(type);
            }
        });
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
        EntityTemplate sourceEntity = gameData.template.get(name);
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

    public Array<Entity> spawnGroup(EntityGroup entityGroup) {
        tEntities.clear();
        Array<EntityTemplate> toSpawn = entityGroup.randomVariant();
        for (EntityTemplate et: toSpawn) {
            Entity entity = spawnEntity(et);
            Character character = engine.createComponent(Character.class);
            character.faction = entityGroup.faction;
            entity.add(character);
            tEntities.add(entity);
        }
        return tEntities;
    }
}
