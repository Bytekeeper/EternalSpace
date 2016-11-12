package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class ApplySteeringSystem extends IteratingSystem {
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

        Weapons weapons = WEAPONS.get(entity);
        WeaponControl weaponControl = WEAPON_CONTROL.get(entity);
        if (weapons != null && weaponControl != null) {
            for (Weapons.Weapon weapon : weapons.weapon) {
                weapon.firing = weaponControl.primaryFire;
            }
        }
        if (entity == game.playerEntity) {
            float volume = MathUtils.lerp(Game.ENGINE_NOISE_VOLUME_LOW, Game.ENGINE_NOISE_VOLUME_HIGH, (Math.abs(steering.thrust) + 0.2f * Math.abs(steering.turn)) / 1.2f);
            float pitch = MathUtils.lerp(1.0f, 2.0f, (Math.abs(steering.thrust) + 0.2f * Math.abs(steering.turn)) / 1.2f);
            Sound snd_engine_noise = game.assets.snd_engine_noise;
            long engine_noise_id = game.engine_noise_id;
            snd_engine_noise.setVolume(engine_noise_id, volume);
            snd_engine_noise.setPitch(engine_noise_id, pitch);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
