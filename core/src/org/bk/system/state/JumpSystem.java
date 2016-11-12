package org.bk.system.state;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.Util;
import org.bk.ai.SteeringUtil;
import org.bk.ai.task.Stop;
import org.bk.data.component.*;
import org.bk.data.component.state.*;
import org.bk.fsm.TransitionListener;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 10.11.2016.
 */
public class JumpSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();
    private final Stop stop = new Stop(null);
    private final Game game;


    public JumpSystem(Game game, int priority) {
        super(Family.all(Steering.class, Transform.class, Movement.class, Jump.class).get(), priority);
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new TransitionListener(Jump.class, ManualControl.class, Land.class, Idle.class));
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Steering steering = STEERING.get(entity);
        Movement movement = MOVEMENT.get(entity);
        Jump jump = JUMP.get(entity);

        Steerable<Vector2> steerable = steering.steerable;
        if (steerable == null) {
            return;
        }

        tv.set(jump.target.position).sub(game.currentSystem.position).angleRad();
        stop.setOwner(steerable).
                setOrientation(tv.angleRad());
        SteeringUtil.applySteering(stop, steerable, steering);

        float targetOrientation = tv.angleRad();
        Transform transform = TRANSFORM.get(entity);
        if (movement.velocity.len2() > Game.ACTION_VELOCITY_THRESHOLD2 ||
                Math.abs(Util.deltaAngle(transform.orientRad, targetOrientation)) > Game.ACTION_DELTA_ANGLE_THRESHOLD) {
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
        if (entity == game.playerEntity) {
            game.assets.snd_hyperdrive_engage.play();
        }
        JumpingOut jumpingOut = getEngine().createComponent(JumpingOut.class);
        jumpingOut.to = jump.target;
        entity.add(jumpingOut);
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
