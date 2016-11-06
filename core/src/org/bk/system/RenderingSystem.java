package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import org.bk.Assets;
import org.bk.Game;
import org.bk.data.Mission;
import org.bk.data.component.*;
import org.bk.graphics.Hud;
import org.bk.graphics.Radar;

import static org.bk.Game.SQRT_2;
import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 15.10.2016.
 */
public class RenderingSystem extends EntitySystem {
    public static final int STAR_BORDER = 200;
    public static final int STAR_BORDER2 = STAR_BORDER * 2;
    private final Assets assets;
    private final SpriteBatch batch;
    private ImmutableArray<Entity> shipEntities;
    private ImmutableArray<Entity> projectileEntities;
    private ImmutableArray<Entity> planetEntities;
    private ImmutableArray<Entity> asteroidEntities;
    private Array<Star> stars = new Array<Star>();
    private Game game;
    private RandomXS128 rnd = new RandomXS128();
    private final Affine2 ta = new Affine2();
    private final Vector2 tv = new Vector2();
    private final Radar radar;

    public RenderingSystem(final Game game, int priority) {
        super(priority);
        this.game = game;
        assets = game.assets;
        batch = game.batch;
        radar = new Radar(game);
        game.hud.addActor(new Hud(game, assets));
        game.inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                tv.set(screenX,  screenY);
                game.viewport.unproject(tv);
                tv.scl(Box2DPhysicsSystem.W2B);
                Entity picked = getEngine().getSystem(Box2DPhysicsSystem.class).pick(tv);
                if (picked != null && STEERING.has(game.player)) {
                    STEERING.get(game.player).selectedEntity = picked;
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void addedToEngine(Engine engine) {
        planetEntities = engine.getEntitiesFor(Family.all(Celestial.class, Transform.class, Body.class).get());
        asteroidEntities = engine.getEntitiesFor(Family.all(Asteroid.class, Transform.class, Body.class).get());
        shipEntities = engine.getEntitiesFor(Family.all(Ship.class, Transform.class, Body.class).get());
        projectileEntities = engine.getEntitiesFor(Family.all(Projectile.class, Transform.class, Body.class).get());
    }

    @Override
    public void update(float deltaTime) {
        batch.begin();
        updateStarBackground();

        for (Entity entity : planetEntities) {
            drawEntityWithBody(entity);
        }
        for (Entity entity : asteroidEntities) {
            drawEntityWithBody(entity);
        }
        for (Entity entity : shipEntities) {
            drawEntityWithBody(entity);
        }
        for (Entity entity : projectileEntities) {
            drawEntityWithBody(entity);
        }
        batch.end();
        game.uiBatch.begin();
        drawHUD();
        game.uiBatch.end();
    }

    private void drawHUD() {
        game.uiBatch.setColor(Color.WHITE);
        drawRadar();
        drawJumpTarget();
        drawMissions();
    }

    private void drawMissions() {
        float y = game.height - assets.hudFont.getLineHeight();
        for (Mission m : game.gameData.activeMission) {
            assets.hudFont.draw(game.uiBatch, m.title, game.width - 200, y);
            y -= assets.hudFont.getLineHeight();
        }
    }

    private void drawJumpTarget() {
        Steering steering = STEERING.get(game.player);
        if (steering != null && steering.jumpTo != null) {
            assets.hudFont.draw(game.uiBatch, steering.jumpTo.name, 220, game.height - assets.hudFont.getLineHeight());
        }
    }

    private void drawEntityWithBody(Entity entity) {
        if (entity.isScheduledForRemoval()) {
            return;
        }
        Transform transform = TRANSFORM.get(entity);
        Body body = BODY.get(entity);

        Vector2 location = transform.location;
        if (!game.viewport.getCamera().frustum.boundsInFrustum(location.x, location.y, 0, body.dimension.x / SQRT_2, body.dimension.y / SQRT_2, 1000)) {
            return;
        }
        Landing landing = LANDING.get(entity);
        if (landing == null) {
            Steering steering = STEERING.get(entity);
            if (steering != null && steering.thrust != 0) {
                Thrusters thrusters = THRUSTERS.get(entity);
                for (Thrusters.Thruster thruster : thrusters.thruster) {
                    tv.set(thruster.offset).rotateRad(transform.orientRad).add(location).rotateRad(thruster.orientRad);
                    float hbx = 8;
                    float hby = 40;
                    batch.draw(assets.textures.get("effect/small+1"), tv.x - hbx, tv.y - hby, hbx, hby,
                            hbx * 2, hby * 2, 1, 1, transform.orientRad * MathUtils.radDeg - 90);
                }
            }
        } else if (landing.landed) {
            return;
        }
        ta.setToTranslation(location);
        ta.rotateRad(transform.orientRad);
        tv.set(body.dimension);
        if (landing != null) {
            float scale = landing.timeRemaining / Landing.LAND_OR_LIFTOFF_DURATION;
            if (landing.landingDirection == Landing.LandingDirection.DEPART) {
                scale = 1 - scale;
            }
            tv.scl(scale);
        }
        TextureRegion textureRegion = assets.textures.get(body.graphics);
        draw(transform, tv, textureRegion);
//        game.assets.debugFont.draw(batch, "" + transform.location, transform.location.x, transform.location.y);
    }

    private void drawRadar() {
        radar.bounds.set(0, game.height - 256, 256, 256);
        radar.position.set(TRANSFORM.get(game.player).location);
        radar.drawBackground();
        for (Entity entity : planetEntities) {
            radar.drawCelestial(entity);
        }
        for (Entity entity : shipEntities) {
            radar.drawShip(entity);
        }
    }

    private void updateStarBackground() {
        int starAmount = Gdx.graphics.getWidth() * Gdx.graphics.getHeight() / 600;
        if (stars.size != starAmount) {
            stars.clear();
            for (int i = 0; i < starAmount; i++) {
                Star star = new Star();
                tv.set(rnd.nextFloat() * (Gdx.graphics.getWidth() + STAR_BORDER2) - STAR_BORDER,
                        rnd.nextFloat() * (Gdx.graphics.getHeight() + STAR_BORDER2) - STAR_BORDER);
                game.viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = brightness();
                stars.add(star);
            }
        }
        for (Star star : stars) {
            tv.set(star.position);
            game.viewport.project(tv);
            if (tv.x < -STAR_BORDER || tv.x > Gdx.graphics.getWidth() + STAR_BORDER) {
                tv.x += Math.signum(Gdx.graphics.getWidth() / 2 - tv.x) * (Gdx.graphics.getWidth() + STAR_BORDER * (1 + rnd.nextFloat()));
                tv.y = rnd.nextFloat() * (Gdx.graphics.getHeight() + STAR_BORDER2) - STAR_BORDER;
                game.viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = brightness();
            } else if (tv.y < -STAR_BORDER || tv.y > Gdx.graphics.getHeight() + STAR_BORDER) {
                tv.x = rnd.nextFloat() * (Gdx.graphics.getWidth() + STAR_BORDER2) - STAR_BORDER;
                tv.y += Math.signum(Gdx.graphics.getHeight() / 2 - tv.y) * (Gdx.graphics.getHeight() + STAR_BORDER * (1 + rnd.nextFloat()));
                tv.y = Gdx.graphics.getHeight() - tv.y;
                game.viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = brightness();
            }
            batch.setColor(star.brightness, star.brightness, star.brightness, 1);
            batch.draw(assets.textures.get("particle"), star.position.x, star.position.y, 3, 3);
        }
        batch.setColor(Color.WHITE);
    }

    private float brightness() {
        return rnd.nextFloat() * 0.5f + 0.2f;
    }

    private void draw(Transform transform, Vector2 dimension, TextureRegion textureRegion) {
        float hbx = dimension.x / 2;
        float hby = dimension.y / 2;
        batch.draw(textureRegion, transform.location.x - hbx, transform.location.y - hby, hbx, hby,
                dimension.x, dimension.y, 1, 1, transform.orientRad * MathUtils.radDeg - 90);
    }

    private class Star {
        final Vector2 position = new Vector2();
        float brightness;
    }
}
