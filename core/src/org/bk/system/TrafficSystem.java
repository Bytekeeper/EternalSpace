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

import static org.bk.data.component.Mapper.CHARACTER;
import static org.bk.data.component.Mapper.MOVEMENT;
import static org.bk.data.component.Mapper.TRANSFORM;

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
        shipEntities = engine.getEntitiesFor(Family.all(Ship.class).get());
        engine.getSystem(SystemPopulateSystem.class).systemChanged.add(new Listener<String>() {
            @Override
            public void receive(Signal<String> signal, String object) {
                Gdx.app.log(TrafficSystem.class.getSimpleName(), "Setting up initial traffic deployment");
                for (SolarSystem.TrafficDefinition td: game.currentSystem.traffic) {
                    int toSpawn = (int) MathUtils.random(120 / td.every);
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
        if (!CHARACTER.has(entity)) {
            Character character = getEngine().createComponent(Character.class);
            character.faction = game.gameData.faction.values().toArray().random();
            entity.add(character);
        }
        AIControlled aiControlled = getEngine().createComponent(AIControlled.class);
        entity.add(aiControlled);
        Transform transform = TRANSFORM.get(entity);
        transform.orientRad = MathUtils.random(-MathUtils.PI, MathUtils.PI);
        if (target != null && (initialDeployment || rnd.nextFloat() < 0.7f)) {
            if (!initialDeployment || rnd.nextFloat() < 0.2f) {
                transform.location.set(TRANSFORM.get(target).location);
                Landing landing = getEngine().createComponent(Landing.class);
                landing.landingDirection = Landing.LandingDirection.DEPART;
                landing.target = target;
                entity.add(landing);
                entity.remove(Physics.class);
                entity.remove(Steering.class);
            } else {
                Movement movement = MOVEMENT.get(entity);
                transform.location.set(TRANSFORM.get(target).location).add(MathUtils.random(-1000, 1000), MathUtils.random(-1000, 1000));
                movement.velocity.setToRandomDirection().scl(MathUtils.random(0, movement.maxVelocity));
            }
            if (rnd.nextFloat() < 0.5f) {
                aiControlled.behaviorTree = game.behaviors.patrol(entity, getEngine());
            } else {
                aiControlled.behaviorTree = game.behaviors.jump(entity, game);
            }
        } else {
            transform.location.set(rnd.nextFloat() * 5000 - 2500, rnd.nextFloat() * 5000 - 2500);
            getEngine().getSystem(JumpingSystem.class).arrive(entity, tv.setToRandomDirection().scl(MathUtils.random(0, 800)),
                    game.gameData.getSystem().random());
            aiControlled.behaviorTree = game.behaviors.land(entity, getEngine());
            entity.remove(Physics.class);
            entity.remove(Steering.class);
        }
    }
}
