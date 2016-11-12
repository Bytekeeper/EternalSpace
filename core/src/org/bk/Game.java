package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.bk.data.EntityGroup;
import org.bk.data.EntityTemplate;
import org.bk.data.GameData;
import org.bk.data.SolarSystem;
import org.bk.data.component.LandingPlace;
import org.bk.data.component.Movement;
import org.bk.data.component.Transform;
import org.bk.data.component.WeaponControl;
import org.bk.data.component.state.Jump;
import org.bk.data.component.state.Land;
import org.bk.data.component.state.ManualControl;
import org.bk.screen.MapScreen;
import org.bk.screen.PlanetScreen;
import org.bk.system.*;
import org.bk.system.state.*;

import static org.bk.data.component.Mapper.*;

public class Game extends com.badlogic.gdx.Game {
    public static final float SQRT_2 = (float) Math.sqrt(2);
    public static final float ACTION_VELOCITY_THRESHOLD2 = 100;
    public static final float ACTION_DELTA_ANGLE_THRESHOLD = 0.01f;
    public static final float JUMP_SCALE = 800;
    public static final float ENGINE_NOISE_VOLUME_LOW = 0.4f;
    public static final float ENGINE_NOISE_VOLUME_HIGH = 0.7f;
    public Viewport viewport;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;
    public ShapeRenderer shape;
    public Assets assets;
    public Behaviors behaviors;
    public Entity playerEntity;
    public int width;
    public int height;
    public Stage stage;
    public Stage hud;
    private EntityFactory entityFactory;
    public PlanetScreen planetScreen;

    public Player player = new Player();
    public SolarSystem currentSystem;
    public GameData gameData;

    public PooledEngine engine;
    private MapScreen mapScreen;
    private float flashTimer, lastFlashTime;

    public InputMultiplexer inputMultiplexer;

    public Signal<Entity> entityDestroyed = new Signal<Entity>();
    public long engine_noise_id;

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shape.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        stage.getViewport().update(width, height);
        hud.getViewport().update(width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        assets = new Assets();
        gameData = assets.gameData;
        behaviors = new Behaviors();
        uiBatch = new SpriteBatch();
        shape = new ShapeRenderer();
        stage = new Stage(new ScreenViewport(), uiBatch);
        hud = new Stage(new ScreenViewport(), uiBatch);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(hud);
        Gdx.input.setInputProcessor(inputMultiplexer);

        batch = new SpriteBatch();
        batch.getProjectionMatrix().setToOrtho2D(-Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        initScreens();

        initWorldEngine();
        entityFactory = new EntityFactory(this);

        assets.gameData.setEngine(engine);
        playerEntity = assets.gameData.player;
        engine.addEntity(playerEntity);

        engine_noise_id = assets.snd_engine_noise.loop(0.1f);
    }

    private void initWorldEngine() {
        engine = new PooledEngine();
        engine.addSystem(new SystemPopulateSystem(this, 0));
        engine.addSystem(new AISystem(1));
        engine.addSystem(new IdleSystem(0));
        engine.addSystem(new LandSystem(2));
        engine.addSystem(new JumpSystem(this, 2));
        engine.addSystem(new LiftingOffSystem(this, 2));
        engine.addSystem(new ManualControlSystem(2));
        engine.addSystem(new WeaponControlSystem(2));
        engine.addSystem(new ApplySteeringSystem(this, 3));
        engine.addSystem(new RenderingSystem(this, 4));
        engine.addSystem(new LifeTimeSystem(4));
        engine.addSystem(new Box2DPhysicsSystem(this, 5));
        engine.addSystem(new ProjectileHitSystem(this, 6));
        engine.addSystem(new WeaponSystem(this, 7));
        engine.addSystem(new HealthSystem(this, 8));
        engine.addSystem(new PersistenceSystem(9));
        engine.addSystem(new SelectionSystem(this, 9));

        engine.addSystem(new AsteroidSystem(this, 9000));

        engine.addSystem(new OrbitingSystem(10000));
        engine.addSystem(new LandingSystem(this, 10000));
        engine.addSystem(new JumpingOutSystem(this, 10000));
        engine.addSystem(new JumpingInSystem(this, 10000));
        engine.addSystem(new BatterySystem(15000));

        engine.addSystem(new TrafficSystem(this, 20000));
    }

    private void initScreens() {
        planetScreen = new PlanetScreen(this);
        mapScreen = new MapScreen(this);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        flashTimer = Math.max(flashTimer - deltaTime, 0);
        float c = MathUtils.lerp(0, 1, flashTimer / lastFlashTime);
        Gdx.gl.glClearColor(c, c, c, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handlePlayerInput();
        Transform transform = TRANSFORM.get(playerEntity);
        viewport.getCamera().position.set(transform.location, 0);
        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        GdxAI.getTimepiece().update(deltaTime);
        engine.update(deltaTime);

        if (LANDED.has(playerEntity)) {
            if (screen != planetScreen) {
                setScreen(planetScreen);
            }
        } else if (screen == planetScreen) {
            setScreen(null);
        }

        uiBatch.begin();
        assets.debugFont.draw(uiBatch, Gdx.graphics.getFramesPerSecond() + " FPS", Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 20);
        assets.debugFont.draw(uiBatch, engine.getEntities().size() + " Entities", Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 40);
        uiBatch.end();
        super.render();
        stage.act();
        stage.draw();
        hud.act();
        hud.draw();
    }

    private float accelTimer = 1;
    private float lastVel;

    private void handlePlayerInput() {
        Movement movement = MOVEMENT.get(playerEntity);
//        accelTimer -= Gdx.graphics.getDeltaTime();
//        if (accelTimer < 0) {
//            Steering steering = STEERING.get(playerEntity);
//            float accel = (movement.velocity.len() - lastVel) / Gdx.graphics.getDeltaTime();
//            accelTimer += 1;
//            com.badlogic.gdx.physics.box2d.Body physicsBody = PHYSICS.get(playerEntity).physicsBody;
//            System.err.println(accel + " " + steering.steerable.getMaxLinearAcceleration() + " " + steering.steerable.getMaxAngularSpeed() + " " + physicsBody.getAngularVelocity() +
//            " " + physicsBody.getMass());
//        }
//        lastVel = movement.velocity.len();
        ManualControl manualControl = MANUAL_CONTROL.get(playerEntity);
        if (manualControl != null) {
            manualControl.thrust = 0;
            manualControl.turn = 0;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            playerControl().thrust = 1;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            playerControl().turn = 1;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            playerControl().turn = -1;
        }
        if (Gdx.input.isKeyPressed(Keys.J) && player.selectedJumpTarget != null) {
            Jump jump = engine.createComponent(Jump.class);
            jump.target = player.selectedJumpTarget;
            playerEntity.add(jump);
        }
        if (Gdx.input.isKeyJustPressed(Keys.M)) {
            if (mapScreen != screen) {
                setScreen(mapScreen);
            } else {
                setScreen(null);
            }
        }
        WeaponControl weaponControl = WEAPON_CONTROL.get(playerEntity);
        if (weaponControl != null) {
            weaponControl.primaryFire = Gdx.input.isKeyPressed(Keys.SPACE);
        }
        if (Gdx.input.isKeyPressed(Keys.L)) {
            Entity planet = null;
            float bestDst2 = Float.POSITIVE_INFINITY;
            Transform playerTransform = TRANSFORM.get(this.playerEntity);
            for (Entity entity : engine.getEntitiesFor(Family.all(LandingPlace.class, Transform.class).get())) {
                float dst2 = TRANSFORM.get(entity).location.dst2(playerTransform.location);
                if (dst2 < bestDst2) {
                    bestDst2 = dst2;
                    planet = entity;
                }
            }

            if (planet != null) {
                Land land = engine.createComponent(Land.class);
                land.on = planet;
                playerEntity.add(land);
            }
        }
    }

    private ManualControl playerControl() {
        ManualControl manualControl = MANUAL_CONTROL.get(playerEntity);
        if (manualControl == null) {
            manualControl = engine.createComponent(ManualControl.class);
            playerEntity.add(manualControl);
        }
        return manualControl;
    }

    public Entity spawn(String entityDefinitionKey, Class<? extends Component>... expectedComponents) {
        return entityFactory.spawnEntity(entityDefinitionKey);
    }

    public Entity spawn(EntityTemplate template) {
        return entityFactory.spawnEntity(template);
    }

    public Array<Entity> spawnGroup(EntityGroup entityGroup) {
        return entityFactory.spawnGroup(entityGroup);
    }

    @Override
    public void setScreen(Screen screen) {
        stage.clear();
        super.setScreen(screen);
    }

    public String msg(String key) {
        return assets.localization.get(key);
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        engine.removeAllEntities();
    }

    public void populateCurrentSystem() {
        entityFactory.spawnSystem(currentSystem);
    }

    public void flash(float time) {
        lastFlashTime = flashTimer = time;
    }
}
