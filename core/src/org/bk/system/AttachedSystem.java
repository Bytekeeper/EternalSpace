package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;
import org.bk.data.component.Attached;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.ATTACHED;
import static org.bk.data.component.Mapper.TRANSFORM;

/**
 * Created by dante on 04.12.2016.
 */
public class AttachedSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();
    private final ObjectSet<Entity> removedSet = new ObjectSet<Entity>();

    public AttachedSystem() {
        super(Family.all(Attached.class, Transform.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(Family.all(Transform.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
            }

            @Override
            public void entityRemoved(Entity entity) {
                removedSet.add(entity);
            }
        });
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        removedSet.clear();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attached attached = ATTACHED.get(entity);
        Entity attachedTo = attached.to;
        if (removedSet.contains(attachedTo)) {
            if (attached.cascadeRemoval) {
                getEngine().removeEntity(entity);
            } else {
                entity.remove(Attached.class);
            }
            return;
        }

        Transform ownerTransform = TRANSFORM.get(attachedTo);
        Transform transform = TRANSFORM.get(entity);
        transform.location.set(attached.offset)
                .rotateRad(ownerTransform.orientRad)
                .add(ownerTransform.location);
        transform.orientRad = (attached.orientRad + ownerTransform.orientRad) % MathUtils.PI2;
    }
}
