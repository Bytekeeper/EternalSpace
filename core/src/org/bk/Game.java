package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.bk.component.*;
import org.bk.component.Character;
import org.bk.screen.PlanetScreen;
import org.bk.system.*;

import static org.bk.component.Mapper.*;

public class Game extends com.badlogic.gdx.Game {
    public static final float SQRT_2 = (float) Math.sqrt(2);
    public Viewport viewport;
    public SpriteBatch batch;
    public SpriteBatch uiBatch;
    public Assets assets;
    public Behaviors behaviors;
    public SolarSystems systems;
    PooledEngine engine;
    public Entity player;
    public int width;
    public int height;
    public Stage stage;
    public PlanetScreen planetScreen;
    public String currentSystem;

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
        uiBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        stage.getViewport().update(width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        assets = new Assets();
        behaviors = new Behaviors();
        systems = new SolarSystems(this);
        batch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), uiBatch);
        planetScreen = new PlanetScreen(this);
        batch.getProjectionMatrix().setToOrtho2D(-Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        engine = new PooledEngine();
        engine.addSystem(new AISystem(this, 0));
        engine.addSystem(new AutopilotSystem(this, 1));
        engine.addSystem(new ApplySteeringSystem(this, 2));
        engine.addSystem(new RenderingSystem(this, 3));
        engine.addSystem(new LifeTimeSystem(4));
        engine.addSystem(new OrbitingSystem(4));
        engine.addSystem(new Box2DPhysicsSystem(5));
        engine.addSystem(new ProjectileHitSystem(6));
        engine.addSystem(new WeaponSystem(this, 7));
        engine.addSystem(new JumpingSystem(this, 8));
        engine.addSystem(new LandingSystem(8));
        engine.addSystem(new HealthSystem(8));
        engine.addSystem(new FactionSystem(1000));
        engine.addSystem(new SystemPopulateSystem(this, 1000));
        engine.addSystem(new AsteroidSystem(this, 1001));
        engine.addSystem(new TrafficSystem(this, 1001));

        assets.gameData.setEngine(engine);

        player = spawn("falcon");
        Persistence persistence = engine.createComponent(Persistence.class);
        persistence.system = "Thorin";
        player.add(persistence);
        player.add(engine.createComponent(Character.class));

        currentSystem = "Thorin";
        populateCurrentSystem();

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handlePlayerInput();
        Transform transform = TRANSFORM.get(player);
        viewport.getCamera().position.set(transform.location, 0);
        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float deltaTime = Gdx.graphics.getDeltaTime();
        GdxAI.getTimepiece().update(deltaTime);
        engine.update(deltaTime);

        Landing landing = LANDING.get(player);
        if (landing != null && landing.landed) {
            if (screen != planetScreen) {
                setScreen(planetScreen);
            }
        } else if (screen == planetScreen) {
            setScreen(null);
        }

        uiBatch.begin();
        assets.debugFont.draw(uiBatch, Gdx.graphics.getFramesPerSecond() + " FPS", Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 20);
        uiBatch.end();
        super.render();
        stage.act();
        stage.draw();
    }

    private void handlePlayerInput() {
        Steering steering = STEERING.get(player);
        if (steering != null) {
            if (Gdx.input.isKeyPressed(Keys.UP)) {
                steering.thrust = 1;
                steering.mode = Steering.SteeringMode.FREE_FLIGHT;
            }
            if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                steering.turn = 1;
                steering.mode = Steering.SteeringMode.FREE_FLIGHT;
            }
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                steering.turn = -1;
                steering.mode = Steering.SteeringMode.FREE_FLIGHT;
            }
            if (Gdx.input.isKeyPressed(Keys.J)) {
                steering.mode = Steering.SteeringMode.JUMPING;
                steering.jumpTo = "Arcos";
            }
            if (Gdx.input.isKeyPressed(Keys.L)) {
                Entity planet = null;
                float bestDst2 = Float.POSITIVE_INFINITY;
                Transform playerTransform = TRANSFORM.get(this.player);
                for (Entity entity : engine.getEntitiesFor(Family.all(Planet.class, Transform.class).get())) {
                    float dst2 = TRANSFORM.get(entity).location.dst2(playerTransform.location);
                    if (dst2 < bestDst2) {
                        bestDst2 = dst2;
                        planet = entity;
                    }
                }

                if (planet != null) {
                    steering.modeTargetEntity = planet;
                    steering.mode = Steering.SteeringMode.LANDING;
                }
            }
            steering.primaryFire = Gdx.input.isKeyPressed(Keys.SPACE);
        }
    }

    public Entity spawn(String entityDefinitionKey, Class<? extends Component>... expectedComponents) {
        return assets.gameData.fabricateEntity(entityDefinitionKey);
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
        assets.gameData.fabricateSystem(currentSystem);
    }
}
