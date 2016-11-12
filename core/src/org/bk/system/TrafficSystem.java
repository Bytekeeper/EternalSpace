package org.bk.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.SolarSystem;
import org.bk.data.component.*;
import org.bk.data.component.Character;
import org.bk.data.component.state.JumpingIn;
import org.bk.data.component.state.LiftingOff;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 18.10.2016.
 */
public class TrafficSystem extends EntitySystem {
    private final Game game;
    private RandomXS128 rnd = new RandomXS128();
    private float nextSpawn;
    private ImmutableArray<Entity> entitiesToLandOn;
    private ImmutableArray<Entity> shipEntities;
    private final Vector2 tv = new Vector2();

    public TrafficSystem(Game game, int priority) {
        super(priority);
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        entitiesToLandOn = engine.getEntitiesFor(Family.all(LandingPlace.class, Transform.class).get());
        shipEntities = engine.getEntitiesFor(Family.all(Controllable.class).get());
        engine.getSystem(SystemPopulateSystem.class).systemChanged.add(new Listener<String>() {
            @Override
            public void receive(Signal<String> signal, String object) {
                Gdx.app.log(TrafficSystem.class.getSimpleName(), "Setting up initial traffic deployment");
                for (SolarSystem.TrafficDefinition td: game.currentSystem.traffic) {
                    int toSpawn = (int) MathUtils.random(120 / td.every);
                    Gdx.app.debug(TrafficSystem.class.getSimpleName(), "Spawning " + toSpawn);
                    while (toSpawn-- > 0) {
                        spawnTraffic(td, true);
                    }
                }
            }
        });
        setSpawnTimer();
    }

    private void spawnTraffic(SolarSystem.TrafficDefinition td, boolean initialDeployment) {
        for (Entity e: game.spawnGroup(td.group)) {
            setupEntity(initialDeployment, e);
        }
    }

    private void setSpawnTimer() {
        nextSpawn = rnd.nextFloat() * 15;
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

    @Override
    public void update(float deltaTime) {
        for (SolarSystem.TrafficDefinition td: game.currentSystem.traffic) {
            if (MathUtils.random() < 1 / td.every * deltaTime) {
                spawnTraffic(td, false);
            }
        }
    }

    private void setupEntity(boolean initialDeployment, Entity entity) {
        Entity target = entitiesToLandOn.random();
        Persistence persistence = getEngine().createComponent(Persistence.class);
        persistence.temporary = true;
        entity.add(persistence);
        Character character = CHARACTER.get(entity);
        if (character == null) {
            character = getEngine().createComponent(Character.class);
            character.faction = game.gameData.faction.values().toArray().random();
            entity.add(character);
        }
        AIControlled aiControlled = getEngine().createComponent(AIControlled.class);
        entity.add(aiControlled);
        Transform transform = TRANSFORM.get(entity);
        transform.orientRad = MathUtils.random(-MathUtils.PI, MathUtils.PI);
        Character targetCharacter = target != null ? CHARACTER.get(target) : null;
        if (target != null && (initialDeployment || rnd.nextFloat() < 0.7f) && (targetCharacter == null || !targetCharacter.faction.isEnemy(character.faction))) {
            if (!initialDeployment || rnd.nextFloat() < 0.2f) {
                transform.location.set(TRANSFORM.get(target).location);
                game.control.setTo(entity, LIFTING_OFF, LiftingOff.class).from = target;
            } else {
                Movement movement = MOVEMENT.get(entity);
                transform.location.set(TRANSFORM.get(target).location).add(MathUtils.random(-1000, 1000), MathUtils.random(-1000, 1000));
                movement.velocity.setToRandomDirection().scl(MathUtils.random(0, movement.maxVelocity));
            }
            if (rnd.nextFloat() < 0.5f) {
                aiControlled.behaviorTree = game.behaviors.patrol(entity, getEngine(), game);
            } else {
                aiControlled.behaviorTree = game.behaviors.jump(entity, game);
            }
        } else {
            transform.location.set(rnd.nextFloat() * 5000 - 2500, rnd.nextFloat() * 5000 - 2500);
            game.control.setTo(entity, JUMPING_IN, JumpingIn.class).from = game.gameData.getSystem().random();
            aiControlled.behaviorTree = game.behaviors.land(entity, getEngine(), game);
        }
    }
}
