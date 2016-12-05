package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.component.*;
import org.bk.data.component.Character;

import java.util.Comparator;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 03.12.2016.
 */
public class BeamHitSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();
    private final Game game;
    private final Comparator<? super Box2DPhysicsSystem.TouchInfo> distanceComparator = new Comparator<Box2DPhysicsSystem.TouchInfo>() {
        @Override
        public int compare(Box2DPhysicsSystem.TouchInfo o1, Box2DPhysicsSystem.TouchInfo o2) {
            float d1 = tv.dst2(o1.collisionPoint);
            float d2 = tv.dst2(o2.collisionPoint);
            return Float.compare(d1, d2);
        }
    };

    public BeamHitSystem(Game game) {
        super(Family.all(Body.class, Beam.class, Transform.class).get());
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Beam beam = BEAM.get(entity);
        Owned owned = OWNED.get(entity);
        Transform transform = TRANSFORM.get(entity);
        Body body = BODY.get(entity);
        tv.set(Vector2.X).rotateRad(transform.orientRad).scl(-body.dimension.y / 2).add(transform.location);
        beam.touched.sort(distanceComparator);
        while (beam.touched.size > 0) {
            Box2DPhysicsSystem.TouchInfo touchInfo = beam.touched.removeIndex(0);
            Entity otherEntity = touchInfo.other;
            for (int i = beam.touched.size - 1; i >= 0; i--) {
                if (beam.touched.get(i).other == otherEntity) {
                    beam.touched.removeIndex(i);
                }
            }
            Owned otherOwned = OWNED.get(otherEntity);
            Character otherCharacter = CHARACTER.get(otherEntity);
            if (otherOwned != null) {
                if (!owned.affiliation.isEnemy(otherOwned.affiliation)) {
                    continue;
                }
            } else if (otherCharacter != null) {
                if (owned.owner == game.playerEntity && otherEntity == game.player.selectedEntity) {
                    owned.affiliation.makeEnemies(otherCharacter.faction);
                }
                if (!otherCharacter.faction.isEnemy(owned.affiliation)) {
                    continue;
                }
            }

            Entity hitEntity = beam.effectEntity;
            if (hitEntity == null) {
                hitEntity = getEngine().createEntity();
                hitEntity.add(getEngine().createComponent(Transform.class));
                Effect effect = getEngine().createComponent(Effect.class);
                hitEntity.add(effect);
                EFFECT.get(hitEntity).effect = beam.hitEffect;
                EFFECT.get(hitEntity).removeEntityWhenDone = true;
                hitEntity.add(getEngine().createComponent(Attached.class));
                getEngine().addEntity(hitEntity);
            }
            Attached attached = ATTACHED.get(hitEntity);
            attached.cascadeRemoval = true;
            attached.orientRad = touchInfo.normal.angleRad() - transform.orientRad;
            attached.offset.set(touchInfo.collisionPoint).sub(transform.location).rotateRad(-transform.orientRad);
            attached.to = entity;
            body.clipY = touchInfo.collisionPoint.dst(tv);
            beam.effectEntity = hitEntity;
            Effect effect = EFFECT.get(hitEntity);
            if (effect.particleEffect != null && effect.particleEffect.isComplete()) {
                beam.effectEntity = null;
            }
            return;
        }
        body.clipY = -1;
        if (beam.effectEntity != null) {
            getEngine().removeEntity(beam.effectEntity);
            beam.effectEntity = null;
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
