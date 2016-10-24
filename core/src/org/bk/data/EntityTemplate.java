package org.bk.data;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.kryo.Kryo;

/**
 * Template for one type of entity.
 */
public class EntityTemplate {
    public Array<Component> components;

    public void applyTo(Kryo kryo, Entity entity, Array<Component> componentSpecializations) {
        ObjectSet<Class<? extends Component>> alreadySet = new ObjectSet<Class<? extends Component>>();
        if (componentSpecializations != null) {
            for (Component component : componentSpecializations) {
                entity.add(kryo.copy(component));
                alreadySet.add(component.getClass());
            }
        }
        for (Component component : components) {
            if (alreadySet.contains(component.getClass())) {
                continue;
            }
            entity.add(kryo.copy(component));
        }
    }
}
