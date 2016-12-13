package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import org.bk.Game;
import org.bk.data.component.Character;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 15.10.2016.
 */
public class WeaponSystem extends IteratingSystem {
    private final Game game;
    private final Vector2 tv = new Vector2();
    private ObjectMap<Weapons.Weapon, Entity> activeBeams = new ObjectMap<Weapons.Weapon, Entity>();
    private ObjectSet<Weapons.Weapon> inactiveWeapons = new ObjectSet<Weapons.Weapon>();

    public WeaponSystem(Game game) {
        super(Family.all(Weapons.class, Transform.class).get());
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        for (Weapons.Weapon weapon : activeBeams.keys()) {
            inactiveWeapons.add(weapon);
        }

        super.update(deltaTime);
        for (Weapons.Weapon w : inactiveWeapons) {
            Entity obsoleteEntity = activeBeams.remove(w);
            getEngine().removeEntity(obsoleteEntity);
        }
        inactiveWeapons.clear();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Weapons weapons = WEAPONS.get(entity);
        for (Weapons.Weapon weapon : weapons.weapon) {
            inactiveWeapons.remove(weapon);
            if (weapon.beam != null) {
                if (!weapon.firing) {
                    Entity beamEntity = activeBeams.remove(weapon);
                    if (beamEntity != null) {
                        getEngine().removeEntity(beamEntity);
                    }
                    continue;
                }
                Entity beamEntity = activeBeams.get(weapon);
                if (beamEntity == null) {
                    beamEntity = spawnBeam(weapon, entity);
//                    SoundComponent soundComponent = getEngine().createComponent(SoundComponent.class);
//                    soundComponent.id = game.assets.snd_beam1.loop();
//                    beamEntity.add(soundComponent);
                    Owned owned = getEngine().createComponent(Owned.class);
                    owned.affiliation = CHARACTER.get(entity).faction;
                    owned.owner = entity;
                    beamEntity.add(owned);
                    activeBeams.put(weapon, beamEntity);
                }
                updateBeam(beamEntity, weapon, entity);
            } else {
                weapon.cooldown = Math.max(0, weapon.cooldown - deltaTime);
                if (weapon.cooldown > 0 || !weapon.firing) {
                    continue;
                }
                weapon.cooldown = weapon.cooldownPerShot;
                if (weapon.projectile != null) {
                    spawnProjectile(weapon, entity);
                }
            }
        }
    }

    private void updateBeam(Entity beamEntity, Weapons.Weapon weapon, Entity owner) {
        Transform sourceTransform = TRANSFORM.get(owner);
        Transform beamTransform = TRANSFORM.get(beamEntity);
        beamTransform.orientRad = (sourceTransform.orientRad + weapon.orientDeg * MathUtils.degreesToRadians) % MathUtils.PI2;
        tv.set(BODY.get(beamEntity).dimension.y / 2, 0).rotateRad(beamTransform.orientRad);
        beamTransform.location
                .set(weapon.offset)
                .rotateRad(sourceTransform.orientRad)
                .add(sourceTransform.location)
                .add(tv);
    }

    private Entity spawnBeam(Weapons.Weapon weapon, Entity owner) {
        if (owner == null) {
            Gdx.app.error(WeaponSystem.class.getSimpleName(), "Weapons without owner");
            return owner;
        }
        Character ownerCharacter = CHARACTER.get(owner);
        Entity beamEntity = game.spawn(weapon.beam);
        Owned owned = getEngine().createComponent(Owned.class);
        owned.owner = owner;
        owned.affiliation = ownerCharacter.faction;
        return beamEntity;
    }

    private void spawnProjectile(Weapons.Weapon weapon, Entity owner) {
        if (owner == null) {
            Gdx.app.error(WeaponSystem.class.getSimpleName(), "Weapons without owner");
            return;
        }
        Transform sourceTransform = TRANSFORM.get(owner);
        Movement sourceMovement = MOVEMENT.get(owner);

        game.assets.snd_shot1.play();

        Character ownerCharacter = CHARACTER.get(owner);
        Entity projectileEntity = game.spawn(weapon.projectile);
        Owned owned = getEngine().createComponent(Owned.class);
        owned.owner = owner;
        owned.affiliation = ownerCharacter.faction;
        projectileEntity.add(owned);
        Transform projectileTransform = TRANSFORM.get(projectileEntity);
        projectileTransform.orientRad = (sourceTransform.orientRad + weapon.orientDeg * MathUtils.degreesToRadians) % MathUtils.PI2;
        tv.set(BODY.get(projectileEntity).dimension.y / 2, 0).rotateRad(projectileTransform.orientRad);
        projectileTransform.location
                .set(weapon.offset)
                .rotateRad(sourceTransform.orientRad)
                .add(sourceTransform.location)
                .add(tv);
        Projectile projectile = PROJECTILE.get(projectileEntity);
        Movement movement = MOVEMENT.get(projectileEntity);
        movement.maxVelocity = 2000;
        movement.velocity.set(Vector2.X).rotateRad(projectileTransform.orientRad).scl(projectile.initialSpeed).add(sourceMovement.velocity);
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
