package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.component.*;
import org.bk.data.SolarSystem;
import org.bk.data.component.state.JumpingOut;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 20.10.2016.
 */
public class JumpingSystem extends IteratingSystem {
    public static final int JUMP_SCALE = 800;
    private final Vector2 tv = new Vector2();
    private final Game game;

    public JumpingSystem(Game game, int priority) {
        super(Family.all(JumpingOut.class, Transform.class, Persistence.class).get(), priority);
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        JumpingOut jumping = JUMPING.get(entity);
        entity.remove(Steering.class);
        entity.remove(Physics.class);
        Transform transform = TRANSFORM.get(entity);
        if (jumping.direction == JumpingOut.JumpDirection.DEPART) {
            transform.orientRad = tv.set(jumping.to.position).sub(game.currentSystem.position).angleRad();
            tv.set(Vector2.X).setAngleRad(transform.orientRad);
            float timePassed = JumpingOut.JUMP_OUT_DURATION / 2 - jumping.timeRemaining;
            float dst = timePassed * timePassed * timePassed * JUMP_SCALE;
            transform.location.set(tv).scl(dst).add(jumping.referencePoint);
        } else {
            float targetOrientation = tv.set(game.currentSystem.position).sub(jumping.to.position).angleRad();
            transform.orientRad = targetOrientation;
            tv.set(Vector2.X).setAngleRad(targetOrientation);
            float dst = jumping.timeRemaining * jumping.timeRemaining * jumping.timeRemaining * JUMP_SCALE;
            transform.location.set(tv).scl(-dst).add(jumping.referencePoint);
        }
        jumping.timeRemaining -= deltaTime;
        if (jumping.timeRemaining < 0) {
            if (jumping.direction == JumpingOut.JumpDirection.DEPART) {
                Persistence persistence = PERSISTENCE.get(entity);
                if (persistence == null || persistence.temporary) {
                    getEngine().removeEntity(entity);
                } else {
                    if (entity == game.playerEntity) {
                        game.flash(0.2f);
                        game.assets.snd_hyperdrive_shutdown.play();
                    }
                    jumping.direction = JumpingOut.JumpDirection.ARRIVE;
                    SolarSystem comingFrom = persistence.system;
                    persistence.system = jumping.to;
                    jumping.to = comingFrom;
                    jumping.timeRemaining = JumpingOut.JUMP_OUT_DURATION / 2;
                    jumping.referencePoint.setToRandomDirection().scl(MathUtils.random(0, 800));
                }
            } else {
                entity.add(getEngine().createComponent(Physics.class));
                entity.add(getEngine().createComponent(Steering.class));
                entity.remove(JumpingOut.class);
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

}
