package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.Util;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class ApplySteeringSystem extends IteratingSystem {
    public static final int ACTION_VELOCITY_THRESHOLD2 = 50;
    public static final double ACTION_DELTA_ANGLE_THRESHOLD = 0.01;
    private final Vector2 tv = new Vector2();
    private final Game game;

    public ApplySteeringSystem(Game game, int priority) {
        super(Family.all(Movement.class, Steering.class, Transform.class).get(), priority);
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Steering steering = STEERING.get(entity);
        Movement movement = MOVEMENT.get(entity);
        Transform transform = TRANSFORM.get(entity);

        tv.set(Vector2.X).rotateRad(transform.orientRad).scl(MathUtils.clamp(steering.thrust, -1, 1) * movement.linearThrust);
        movement.linearAccel.add(tv);
        movement.angularAccel += MathUtils.clamp(steering.turn, -1, 1) * movement.angularThrust;

        switch (steering.mode) {
            case LANDING:
                tryLanding(entity, movement);
                break;
            case JUMPING:
                tryJumping(entity, movement, steering, transform);
                break;
        }

        Weapons weapons = WEAPONS.get(entity);
        if (weapons != null) {
            for (Weapons.Weapon weapon : weapons.weapon) {
                weapon.firing = steering.primaryFire;
            }
        }
    }

    private void tryJumping(Entity entity, Movement movement, Steering steering, Transform transform) {
        tv.set(steering.jumpTo.position).sub(game.currentSystem.position).angleRad();
        float targetOrientation = tv.angleRad();
        if (movement.velocity.len2() > ACTION_VELOCITY_THRESHOLD2 ||
                Math.abs(Util.deltaAngle(transform.orientRad, targetOrientation)) > ACTION_DELTA_ANGLE_THRESHOLD) {
            return;
        }
        Battery battery = BATTERY.get(entity);
        JumpDrive jumpDrive = JUMP_DRIVE.get(entity);
        if (jumpDrive == null) {
            return;
        }
        if (battery != null) {
            if (battery.capacity < jumpDrive.powerCost) {
                return;
            }
            battery.capacity -= jumpDrive.powerCost;
        }
        if (entity == game.player) {
            game.assets.snd_hyperdrive_engage.play();
        }
        getEngine().getSystem(JumpingSystem.class).depart(entity, transform.location, steering.jumpTo);
    }

    private void tryLanding(Entity entity, Movement movement) {
        if (movement.velocity.len2() > ACTION_VELOCITY_THRESHOLD2) {
            return;
        }
        Touching touching = TOUCHING.get(entity);
        if (touching != null) {
            for (Entity e : touching.touchList) {
                if (CELESTIAL.has(e)) {
                    Landing landing = getEngine().createComponent(Landing.class);
                    landing.landingDirection = Landing.LandingDirection.LANDING;
                    landing.target = e;
                    entity.add(landing);
                }
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
