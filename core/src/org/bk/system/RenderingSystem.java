package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import org.bk.Assets;
import org.bk.Game;
import org.bk.data.Mission;
import org.bk.data.component.*;
import org.bk.data.component.Character;
import org.bk.data.component.state.Landing;
import org.bk.data.component.state.LiftingOff;
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
    private static final boolean AI_DEBUG = false;
    private final Assets assets;
    private final SpriteBatch batch;
    private ImmutableArray<Entity> shipEntities;
    private ImmutableArray<Entity> projectileEntities;
    private ImmutableArray<Entity> planetEntities;
    private ImmutableArray<Entity> asteroidEntities;
    private Array<Star> stars = new Array<Star>();
    private Game game;
    private RandomXS128 rnd = new RandomXS128();
    private final Vector2 tv = new Vector2();
    private final Vector2 tv2 = new Vector2();
    private final Radar radar;
    private GlyphLayout glyphLayout = new GlyphLayout();
    private ObjectMap<Weapons.Weapon, ParticleEffectPool.PooledEffect> muzzles = new ObjectMap<Weapons.Weapon, ParticleEffectPool.PooledEffect>();

    public RenderingSystem(final Game game) {
        this.game = game;
        assets = game.assets;
        batch = game.batch;
        radar = new Radar(game);
        game.hud.addActor(new Hud(game, assets));
    }

    @Override
    public void addedToEngine(Engine engine) {
        planetEntities = engine.getEntitiesFor(Family.all(Celestial.class, Transform.class, Body.class).get());
        asteroidEntities = engine.getEntitiesFor(Family.all(Asteroid.class, Transform.class, Body.class).get());
        shipEntities = engine.getEntitiesFor(Family.all(Controllable.class, Transform.class, Body.class).get());
        projectileEntities = engine.getEntitiesFor(Family.all(Projectile.class, Transform.class, Body.class).get());
    }

    @Override
    public void update(float deltaTime) {
        batch.begin();
        updateStarBackground();

        for (Entity entity : planetEntities) {
            drawEntityWithBody(entity, deltaTime);
            Transform transform = TRANSFORM.get(entity);
            tv.set(game.viewport.getCamera().position.x, game.viewport.getCamera().position.y);
            if (transform.location.dst2(tv) < 400 * 400) {
                float shade = MathUtils.clamp((450 * 450 - transform.location.dst2(tv)) / 400 / 400, 0, 1);
                batch.setColor(1, 1, 1, shade);
                drawCelestialInfo(entity);
                batch.setColor(Color.WHITE);
            }
        }
        for (Entity entity : asteroidEntities) {
            drawEntityWithBody(entity, deltaTime);
        }
        for (Entity entity : shipEntities) {
            drawEntityWithBody(entity, deltaTime);
            if (game.player.selectedEntity == entity) {
                Character character = CHARACTER.get(entity);
                if (character != null) {
                    batch.setColor(character.faction.color);
                }
                drawMarkerAround(entity);
                batch.setColor(Color.WHITE);
            }
        }
        for (Entity entity : projectileEntities) {
            drawEntityWithBody(entity, deltaTime);
        }
        batch.end();
        game.uiBatch.begin();
        drawHUD();
        game.uiBatch.end();
    }

    private void drawCelestialInfo(Entity entity) {
        Name name = NAME.get(entity);
        if (name == null) {
            return;
        }
        Transform transform = drawMarkerAround(entity);
        glyphLayout.setText(assets.hudFont, name.name);
        tv.x = transform.location.x - glyphLayout.width / 2;
        tv.y = transform.location.y - tv.y - glyphLayout.height;

        assets.hudFont.draw(batch, name.name, tv.x, tv.y);
    }

    private Transform drawMarkerAround(Entity entity) {
        Transform transform = TRANSFORM.get(entity);
        Body body = BODY.get(entity);
        tv.set(body.dimension.len() * 0.7f, 0).rotate(45);
        tv2.set(transform.location).sub(4, 16);
        batch.draw(assets.textures.get("ui/marker"), tv2.x - tv.x, tv2.y - tv.y,
                4, 16, 8, 32, 1, 1, -45);
        batch.draw(assets.textures.get("ui/marker"), tv2.x + tv.x, tv2.y - tv.y,
                4, 16, 8, 32, 1, 1, 45);
        batch.draw(assets.textures.get("ui/marker"), tv2.x - tv.x, tv2.y + tv.y,
                4, 16, 8, 32, 1, 1, 45);
        batch.draw(assets.textures.get("ui/marker"), tv2.x + tv.x, tv2.y + tv.y,
                4, 16, 8, 32, 1, 1, -45);
        return transform;
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
        if (game.player.selectedJumpTarget != null) {
            assets.hudFont.draw(game.uiBatch, game.player.selectedJumpTarget.name, 220, game.height - assets.hudFont.getLineHeight());
        }
    }

    private void drawEntityWithBody(Entity entity, float delta) {
        if (entity.isScheduledForRemoval()) {
            return;
        }
        Transform transform = TRANSFORM.get(entity);
        Body body = BODY.get(entity);

        Vector2 location = transform.location;
        if (!game.viewport.getCamera().frustum.boundsInFrustum(location.x, location.y, 0, body.dimension.x / SQRT_2, body.dimension.y / SQRT_2, 1000)) {
            return;
        }
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
        if (LANDED.has(entity)) {
            return;
        }
        tv.set(body.dimension);
        Landing landing = LANDING.get(entity);
        LiftingOff liftingOff = LIFTING_OFF.get(entity);
        if (landing != null) {
            float scale = landing.timeRemaining / Landing.LANDING_DURATION;
            tv.scl(scale);
        } else if (liftingOff != null) {
            float scale = 1 - liftingOff.timeRemaining / liftingOff.LIFTOFF_DURATION;
            tv.scl(scale);
        }
        TextureRegion textureRegion = assets.textures.get(body.graphics);
        draw(transform, tv, textureRegion);
        Weapons weapons = WEAPONS.get(entity);
        if (weapons != null) {
            for (Weapons.Weapon w : weapons.weapon) {
                ParticleEffectPool.PooledEffect muzzleEffect = muzzles.get(w);
                if(muzzleEffect != null) {
                    tv.set(w.offset).rotateRad(transform.orientRad).add(transform.location);
                    muzzleEffect.setPosition(tv.x, tv.y);
                    muzzleEffect.draw(batch, delta);
                    if (muzzleEffect.isComplete()) {
                        muzzleEffect.free();
                        muzzles.remove(w);
                        muzzleEffect = null;
                    }
                }
                 if (!w.firing || w.muzzleEffect == null) {
                    continue;
                }
                if (muzzleEffect == null) {
                    muzzleEffect = assets.effects.get(w.muzzleEffect).obtain();
                    float rotate = transform.orientRad * MathUtils.radiansToDegrees - 90;
                    for (ParticleEmitter particleEmitter : muzzleEffect.getEmitters()) {
                        ParticleEmitter.ScaledNumericValue angle = particleEmitter.getAngle();
                        angle.setLow(angle.getLowMin() + rotate, angle.getLowMax() + rotate);
                        angle.setHigh(angle.getHighMin() + rotate, angle.getHighMax() + rotate);
                    }

                    muzzles.put(w, muzzleEffect);
                }
            }
        }

//        game.assets.debugFont.draw(batch, "" + transform.location, transform.location.x, transform.location.y);
        if (AI_DEBUG && AI_CONTROLLED.has(entity)) {
//            Component state = game.control.getState(entity);
//            if (state != null) {
//                assets.debugFont.draw(batch, String.format("state: %s", state.getClass().getSimpleName()), transform.location.x, transform.location.y - 20);
//            }
            AIControlled aiControlled = AI_CONTROLLED.get(entity);
            if (aiControlled.behaviorTree != null) {
                renderBehaviorTree(aiControlled.behaviorTree, transform.location.x, transform.location.y - 20);
            }
        }
    }

    private float renderBehaviorTree(Task<Entity> task, float x, float y) {
        y -= 20;
        assets.debugFont.draw(batch, String.format("%s: %s", task.getClass().getSimpleName(), task.getStatus()), x, y);
        for (int i = 0; i < task.getChildCount(); i++) {
            y = renderBehaviorTree(task.getChild(i), x + 20, y);
        }
        return y;
    }

    private void drawRadar() {
        radar.bounds.set(0, game.height - 256, 256, 256);
        radar.position.set(TRANSFORM.get(game.playerEntity).location);
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
