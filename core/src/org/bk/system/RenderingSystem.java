package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import org.bk.Assets;
import org.bk.BiasedScaledNumericValueDelegate;
import org.bk.Game;
import org.bk.data.Mission;
import org.bk.data.component.*;
import org.bk.data.component.Character;
import org.bk.data.component.state.Landing;
import org.bk.data.component.state.LiftingOff;
import org.bk.graphics.Radar;
import org.bk.ui.BackgroundStars;
import org.bk.ui.Hud;

import static org.bk.Game.SQRT_2;
import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 15.10.2016.
 */
public class RenderingSystem extends EntitySystem {
    private static final boolean AI_DEBUG = false;
    private final Assets assets;
    private final SpriteBatch batch;
    private ImmutableArray<Entity> shipEntities;
    private ImmutableArray<Entity> projectileEntities;
    private ImmutableArray<Entity> planetEntities;
    private ImmutableArray<Entity> asteroidEntities;
    private ImmutableArray<Entity> effectEntities;
    private Game game;
    private final Vector2 tv = new Vector2();
    private final Vector2 tv2 = new Vector2();
    private final Radar radar;
    private GlyphLayout glyphLayout = new GlyphLayout();
    private ObjectMap<Weapons.Weapon, ParticleEffectPool.PooledEffect> muzzles = new ObjectMap<Weapons.Weapon, ParticleEffectPool.PooledEffect>();
    private ObjectMap<Thrusters.Thruster, ParticleEffectPool.PooledEffect> thrusters = new ObjectMap<Thrusters.Thruster, ParticleEffectPool.PooledEffect>();
    private ObjectMap<Entity, ParticleEffectPool.PooledEffect> effects = new ObjectMap<Entity, ParticleEffectPool.PooledEffect>();
    private BackgroundStars backgroundStars;

    public RenderingSystem(final Game game) {
        this.game = game;
        assets = game.assets;
        batch = game.batch;
        radar = new Radar(game);
        game.hud.addActor(new Hud(game, assets));
        backgroundStars = new BackgroundStars(game.viewport, assets.textures.get("particle"));
    }

    @Override
    public void addedToEngine(Engine engine) {
        planetEntities = engine.getEntitiesFor(Family.all(Celestial.class, Transform.class, Body.class).get());
        asteroidEntities = engine.getEntitiesFor(Family.all(Asteroid.class, Transform.class, Body.class).get());
        shipEntities = engine.getEntitiesFor(Family.all(Controllable.class, Transform.class, Body.class).get());
        projectileEntities = engine.getEntitiesFor(Family.all(Projectile.class, Transform.class, Body.class).get());
        effectEntities = engine.getEntitiesFor(Family.all(Effect.class, Transform.class).get());
    }

    @Override
    public void update(float deltaTime) {
        batch.begin();
        backgroundStars.setDimensions(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundStars.draw(batch);

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
        renderParticleEffects(deltaTime);
        batch.end();
        game.uiBatch.begin();
        drawHUD();
        game.uiBatch.end();
    }

    private void renderParticleEffects(float deltaTime) {
        for (Entity e: effectEntities) {
            ParticleEffectPool.PooledEffect pooledEffect = effects.get(e);
            if (pooledEffect == null) {
                Effect effect = EFFECT.get(e);
                pooledEffect = assets.effects.get(effect.effect).obtain();
                effects.put(e, pooledEffect);
            }
            Transform transform = TRANSFORM.get(e);
            pooledEffect.setPosition(transform.location.x, transform.location.y);
            float rotate = radToDegForRender(transform);
            BiasedScaledNumericValueDelegate.setBias(pooledEffect.getEmitters(), rotate);
            pooledEffect.draw(batch, deltaTime);
            if (pooledEffect.isComplete()) {
                pooledEffect.free();
                effects.remove(e);
                getEngine().removeEntity(e);
            }
        }
    }

    private float radToDegForRender(Transform transform) {
        return transform.orientRad * MathUtils.radiansToDegrees - 90;
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
        if (steering != null) {
            boolean fireThrusters = steering.thrust != 0;
            Thrusters thrusters = THRUSTERS.get(entity);
            for (Thrusters.Thruster thruster : thrusters.thruster) {
                ParticleEffectPool.PooledEffect thrusterEffect = this.thrusters.get(thruster);
                if (thrusterEffect != null) {
                    tv.set(thruster.offset).rotateRad(transform.orientRad).add(transform.location);
                    float rotate = radToDegForRender(transform) + thruster.orientDeg;
                    thrusterEffect.setPosition(tv.x, tv.y);
                    BiasedScaledNumericValueDelegate.setBias(thrusterEffect.getEmitters(), rotate);
                    thrusterEffect.draw(batch, delta);
                    if (!fireThrusters) {
                        thrusterEffect.free();
                        this.thrusters.remove(thruster);
                        thrusterEffect = null;
                    }
                }
                if (thrusterEffect == null && fireThrusters) {
                    thrusterEffect = assets.effects.get(thruster.thrusterEffect).obtain();
                    this.thrusters.put(thruster, thrusterEffect);

                }
//                tv.set(thruster.offset).rotateRad(transform.orientDeg).add(location).rotateRad(thruster.orientDeg);
//                float hbx = 8;
//                float hby = 40;
//                batch.draw(assets.textures.get("effect/small+1"), tv.x - hbx, tv.y - hby, hbx, hby,
//                        hbx * 2, hby * 2, 1, 1, transform.orientDeg * MathUtils.radDeg - 90);
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
                if (muzzleEffect != null) {
                    tv.set(w.offset).rotateRad(transform.orientRad).add(transform.location);
                    float rotate = radToDegForRender(transform) + w.orientDeg;
                    BiasedScaledNumericValueDelegate.setBias(muzzleEffect.getEmitters(), rotate);
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

    private void draw(Transform transform, Vector2 dimension, TextureRegion textureRegion) {
        float hbx = dimension.x / 2;
        float hby = dimension.y / 2;
        batch.draw(textureRegion, transform.location.x - hbx, transform.location.y - hby, hbx, hby,
                dimension.x, dimension.y, 1, 1, transform.orientRad * MathUtils.radDeg - 90);
    }

}
