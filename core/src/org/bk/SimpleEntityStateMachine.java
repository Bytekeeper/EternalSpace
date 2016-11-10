package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 10.11.2016.
 */
public class SimpleEntityStateMachine {
    private final PooledEngine engine;
    private final Array<Class<? extends Component>> oneOf;

    public SimpleEntityStateMachine(PooledEngine engine, Class<? extends Component>... oneOf) {
        this.engine = engine;
        this.oneOf = new Array<Class<? extends Component>>(oneOf);
    }

    public <T extends Component> T setTo(Entity entity, ComponentMapper<T> mapper, Class<T> component) {
        T result = mapper.get(entity);
        if (result == null) {
            for (Class<? extends Component> toRemove : oneOf) {
                if (toRemove != component) {
                    entity.remove(toRemove);
                }
            }
            result = engine.createComponent(component);
        }
        entity.add(result);
        return result;
    }
}
