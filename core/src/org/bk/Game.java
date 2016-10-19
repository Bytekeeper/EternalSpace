package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.bk.ai.SteeringUtil;
import org.bk.component.*;
import org.bk.system.*;

import java.util.Map;

import static org.bk.component.Mapper.*;

public class Game extends ApplicationAdapter {
    public Viewport viewport;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;
    public Assets assets;
    public Behaviors behaviors;
    public EntityFactory entityFactory;
    PooledEngine engine;
    public Entity player;
    private Entity planet;
    public int width;
    public int height;

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        assets = new Assets();
        behaviors = new Behaviors();
        batch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        batch.getProjectionMatrix().setToOrtho2D(-Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        engine = new PooledEngine();
        engine.addSystem(new AISystem(this, 1));
        engine.addSystem(new SteeringSystem(2));
        engine.addSystem(new RenderingSystem(this, 3));
        engine.addSystem(new LifeTimeSystem(4));
        engine.addSystem(new Box2DPhysicsSystem(5));
        engine.addSystem(new ProjectileHitSystem(6));
        engine.addSystem(new WeaponSystem(this, 7));
        engine.addSystem(new TrafficSystem(this, 7));
        engine.addSystem(new AsteroidSystem(this, 7));
        engine.addSystem(new LandingSystem(8));
        engine.addSystem(new HealthSystem(8));

        entityFactory = new EntityFactory(engine);

        planet = spawn("planet");
        TRANSFORM.get(spawn("planet")).location.set(2000, 1000);

        player = spawn("ship");
        for (int i = 0; i < 0; i++) {
            Entity enemy = spawn("ship");
            TRANSFORM.get(enemy).location.x += i * 120;
            Steering steering = STEERING.get(enemy);

            AIControlled aiControlled = new AIControlled();
            aiControlled.behaviorTree = behaviors.land(enemy);
            steering.targetEntity = planet;
            steering.targetLocation = SteeringUtil.toLocation(TRANSFORM.get(planet).location);
//        Arrive<Vector2> arrive = new Arrive<Vector2>(aiControlled.steerable, SteeringUtil.toLocation(new Vector2(2000, 3000)));
//        arrive.setTimeToTarget(0.01f);
//        arrive.setDecelerationRadius(1000);
//        arrive.setArrivalTolerance(20);
//        aiControlled.behavior = arrive;
            enemy.add(aiControlled);
        }
    }

    private BehaviorTree<Entity> playerAutopilot;

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Steering steering = STEERING.get(player);
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            steering.thrust = 1;
            playerAutopilot = null;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            steering.turn = 1;
            playerAutopilot = null;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            steering.turn = -1;
            playerAutopilot = null;
        }
        if (Gdx.input.isKeyPressed(Keys.L)) {
            STEERING.get(player).targetLocation = SteeringUtil.toLocation(TRANSFORM.get(planet).location);
            STEERING.get(player).targetEntity = planet;
            playerAutopilot = behaviors.land(player);
        }
        STEERING.get(player).primaryFire = Gdx.input.isKeyPressed(Keys.SPACE);

        if (playerAutopilot != null) {
            playerAutopilot.setObject(player);
            playerAutopilot.step();
        }
        Transform transform = TRANSFORM.get(player);
        viewport.getCamera().position.set(transform.location, 0);
        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float deltaTime = Gdx.graphics.getDeltaTime();
        GdxAI.getTimepiece().update(deltaTime);
        engine.update(deltaTime);
        uiBatch.begin();
        assets.debugFont.draw(uiBatch, Gdx.graphics.getFramesPerSecond() + " FPS", Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 20);
        uiBatch.end();
    }

    public Entity spawn(String entityDefinitionKey, Class<? extends Component>... expectedComponents) {
        return spawn(entityFactory.key(entityDefinitionKey), expectedComponents);
    }

    public Entity spawn(EntityFactory.EntityDefinitionKey entityDefinitionKey, Class<? extends Component>... expectedComponents) {
        Entity entity = entityFactory.create(entityDefinitionKey);
        engine.addEntity(entity);
        return entity;
    }

    @Override
    public void dispose() {
        batch.dispose();
        engine.removeAllEntities();
    }
}
