package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import org.bk.Assets;
import org.bk.Game;
import org.bk.component.*;
import org.bk.graphics.Radar;

import static org.bk.component.Mapper.*;

/**
 * Created by dante on 15.10.2016.
 */
public class RenderingSystem extends EntitySystem {
    private final Assets assets;
    private final SpriteBatch batch;
    private ImmutableArray<Entity> shipEntities;
    private ImmutableArray<Entity> mountedEntities;
    private ImmutableArray<Entity> projectileEntities;
    private ImmutableArray<Entity> planetEntities;
    private Array<Star> stars = new Array<Star>();
    private Game game;
    private RandomXS128 rnd = new RandomXS128();
    private final Affine2 ta = new Affine2();
    private final Vector2 tv = new Vector2();
    private final Radar radar;

    public RenderingSystem(Game game, int priority) {
        super(priority);
        this.game = game;
        assets = game.assets;
        batch = game.batch;
        radar = new Radar(game);
        radar.bounds.set(0, 300, 200, 100);
    }

    @Override
    public void addedToEngine(Engine engine) {
        planetEntities = engine.getEntitiesFor(Family.all(Planet.class, Transform.class, Body.class).get());
        shipEntities = engine.getEntitiesFor(Family.all(Ship.class, Transform.class, Body.class).get());
        mountedEntities = engine.getEntitiesFor(Family.all(VisibleAddon.class, Transform.class).get());
        projectileEntities = engine.getEntitiesFor(Family.all(Projectile.class, Transform.class, Body.class).get());
    }

    @Override
    public void update(float deltaTime) {
        batch.begin();
        updateStarBackground();

        for (Entity entity: planetEntities) {
            drawEntityWithBody(entity, assets.planet_placeholder);
        }
        for (Entity entity: shipEntities) {
            drawEntityWithBody(entity, assets.ship_placeholder);
        }
        for (Entity entity: mountedEntities) {
            Transform transform = TRANSFORM.get(entity);
            VisibleAddon visibleAddon = VISIBLE_ADDON.get(entity);
            ta.setToTranslation(transform.location);
            ta.rotateRad(transform.orientRad);
            Vector2 dimension = visibleAddon.dimension;
            draw(transform, dimension, assets.ship_placeholder);
        }
        for (Entity entity: projectileEntities) {
            drawEntityWithBody(entity, assets.ship_placeholder);
        }
        batch.end();
        game.uiBatch.begin();
        drawRadar();
        game.uiBatch.end();
    }

    private void drawEntityWithBody(Entity entity, TextureRegion textureRegion) {
        Transform transform = TRANSFORM.get(entity);
        Body body = BODY.get(entity);
        ta.setToTranslation(transform.location);
        ta.rotateRad(transform.orientRad);
        Vector2 dimension = body.dimension;
        draw(transform, dimension, textureRegion);
    }

    private void drawRadar() {
        radar.position.set(TRANSFORM.get(game.player).location);
        radar.drawBackground();
        for (Entity entity: shipEntities) {
            radar.drawShip(entity);
        }
    }

    private void updateStarBackground() {
        int starAmount = Gdx.graphics.getWidth() * Gdx.graphics.getHeight() / 2000;
        if (stars.size != starAmount) {
            stars.clear();
            for (int i = 0; i < starAmount; i++) {
                Star star = new Star();
                tv.set(rnd.nextFloat() * (Gdx.graphics.getWidth() + 100) - 50,
                        rnd.nextFloat() * (Gdx.graphics.getHeight() + 100) - 50);
                game.viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = rnd.nextFloat() * 0.7f + 0.3f;
                stars.add(star);
            }
        }
        for (Star star: stars) {
            tv.set(star.position);
            game.viewport.project(tv);
            if (tv.x < -50 || tv.x > Gdx.graphics.getWidth() + 50) {
                tv.x = Math.signum(Gdx.graphics.getWidth() / 2 - tv.x) * (Gdx.graphics.getWidth() / 2 + 50 * rnd.nextFloat()) + Gdx.graphics.getWidth() / 2;
                tv.y = rnd.nextFloat() * (Gdx.graphics.getHeight() + 100) - 50;
                game.viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = rnd.nextFloat() * 0.7f + 0.3f;
            } else if (tv.y < -50 || tv.y > Gdx.graphics.getHeight() + 50) {
                tv.x = rnd.nextFloat() * (Gdx.graphics.getWidth() + 100) - 50;
                tv.y = -Math.signum(Gdx.graphics.getHeight() / 2 - tv.y) * (Gdx.graphics.getHeight() / 2 + 50 * rnd.nextFloat()) + Gdx.graphics.getHeight() / 2;
                game.viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = rnd.nextFloat() * 0.7f + 0.3f;
            }
            batch.setColor(star.brightness, star.brightness, star.brightness, 1);
            batch.draw(assets.bg_star, star.position.x, star.position.y, 3, 3);
        }
        batch.setColor(Color.WHITE);
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
