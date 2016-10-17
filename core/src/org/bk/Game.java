package org.bk;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.bk.ai.Arrive;
import org.bk.ai.Attack;
import org.bk.ai.SteeringUtil;
import org.bk.component.*;
import org.bk.spec.ProjectileSpec;
import org.bk.system.*;

import static org.bk.component.Mapper.*;

public class Game extends ApplicationAdapter {
    public Viewport viewport;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;
    public Assets assets;
    PooledEngine engine;
    public Entity player;
    private Entity planet;

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        assets = new Assets();
        batch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        batch.getProjectionMatrix().setToOrtho2D(-Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        engine = new PooledEngine();
        engine.addSystem(new RenderingSystem(this, 1));
        engine.addSystem(new AISystem(this, 2));
        engine.addSystem(new SteeringSystem(3));
        engine.addSystem(new LifeTimeSystem(4));
        engine.addSystem(new Box2DPhysicsSystem(5));
        engine.addSystem(new ProjectileHitSystem(6));
        engine.addSystem(new WeaponSystem(7));
        engine.addSystem(new HealthSystem(8));

        planet = addPlanet();

        player = addShip();
        for (int i = 0; i < 10; i++) {
            Entity enemy = addShip();
            TRANSFORM.get(enemy).location.x += i * 120;
            AIControlled aiControlled = new AIControlled();
            aiControlled.steerable = SteeringUtil.toSteeringBehavior(enemy);
            Attack attack = new Attack(aiControlled.steerable, SteeringUtil.toSteeringBehavior(player));
            attack.setMaxMatchDistance(20);
            aiControlled.behavior = attack;
//        Arrive<Vector2> arrive = new Arrive<Vector2>(aiControlled.steerable, SteeringUtil.toLocation(new Vector2(2000, 3000)));
//        arrive.setTimeToTarget(0.01f);
//        arrive.setDecelerationRadius(1000);
//        arrive.setArrivalTolerance(20);
//        aiControlled.behavior = arrive;
            enemy.add(aiControlled);
        }
    }

    private Entity addPlanet() {
        Entity entity = engine.createEntity();
        Body body = engine.createComponent(Body.class);
        body.dimension.set(190, 190);
        entity.add(body);
        entity.add(engine.createComponent(Planet.class));
        Transform transform = engine.createComponent(Transform.class);
        transform.location.set(100, 50);
        entity.add(transform);
        engine.addEntity(entity);
        return entity;
    }

    private Entity addShip() {
        Entity ship = new Entity();
        Body body = new Body();
        body.dimension.set(35, 40);
        ship.add(body);
        Transform transform = new Transform();
        transform.location.set(400, 300);
        transform.orientRad = 0;
        ship.add(transform);
        ship.add(new Ship());
        Movement movement = new Movement();
        movement.maxVelocity = 500;
        movement.angularThrust = 3000;
        movement.linearThrust = 20000;
        ship.add(movement);
        Health health = new Health();
        health.hull = 100;
        health.shields = 100;
        ship.add(health);
        ship.add(new Steering());

        Weapons.Weapon weapon = new Weapons.Weapon();
        weapon.cooldownPerShot = 0.1f;
        weapon.projectileSpec = new ProjectileSpec();
        weapon.projectileSpec.dimension.set(20, 30);
        weapon.projectileSpec.initialSpeed = 1000;
        weapon.projectileSpec.lifeTime = 2;
        weapon.projectileSpec.yield = 10;
        Weapons weapons = new Weapons();
        weapons.weapons.add(weapon);
        ship.add(weapons);

        engine.addEntity(ship);
        return ship;
    }

    private SteeringBehavior<Vector2> playerBehavior;
    private Steerable<Vector2> playerSteerable;

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Steering steering = STEERING.get(player);
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            steering.thrust = 1;
            playerBehavior = null;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            steering.turn = 1;
            playerBehavior = null;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            steering.turn = -1;
            playerBehavior = null;
        }
        if (Gdx.input.isKeyPressed(Keys.L)) {
            playerSteerable = SteeringUtil.toSteeringBehavior(player);
            Arrive<Vector2> arrive = new Arrive<Vector2>(playerSteerable, SteeringUtil.toSteeringBehavior(planet));
            arrive.setArrivalTolerance(20);
            playerBehavior = arrive;
        }
        if (playerBehavior != null) {
            SteeringUtil.applySteering(playerBehavior, playerSteerable, STEERING.get(player));
        }
        ImmutableArray<Entity> weapons = engine.getEntitiesFor(Family.all(Weapons.class).get());
        for (Entity e: weapons) {
            for (Weapons.Weapon weapon: WEAPON.get(e).weapons) {
                weapon.firing = Gdx.input.isKeyPressed(Keys.SPACE);
            }
        }
        Transform transform = TRANSFORM.get(player);
        viewport.getCamera().position.set(transform.location, 0);
        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        engine.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        batch.dispose();
        engine.removeAllEntities();
    }
}
