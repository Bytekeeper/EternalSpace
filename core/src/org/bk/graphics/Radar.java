package org.bk.graphics;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import org.bk.Assets;
import org.bk.Game;
import org.bk.data.component.Celestial;
import org.bk.data.component.Character;
import org.bk.data.component.state.JumpingIn;
import org.bk.data.component.state.JumpingOut;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class Radar {
    private static final float CELESTIAL_SCALE = 1f;
    private static final float BORDER = 20;
    public final Rectangle bounds = new Rectangle();
    public final Vector2 position = new Vector2();
    private final Assets assets;
    private final Game game;
    private SpriteBatch batch;
    private final Vector2 tv = new Vector2();
    private final Vector2 tv2 = new Vector2();

    public Radar(Game game) {
        this.batch = game.uiBatch;
        assets = game.assets;
        this.game = game;
    }

    public void drawBackground() {
        batch.draw(assets.textures.get("ui/radar_placeholder"), bounds.x, bounds.y, bounds.getWidth(), bounds.getHeight());
    }

    public void drawShip(Entity entity) {
        determineDrawPosition(entity);
        JumpingOut jumpingOut = JUMPING_OUT.get(entity);
        JumpingIn jumpingIn = JUMPING_IN.get(entity);
        if (jumpingOut != null || jumpingIn != null) {
            float t;
            if (jumpingOut != null) {
                t = JumpingOut.JUMP_OUT_DURATION - jumpingOut.timeRemaining;
            } else {
                t = jumpingIn.timeRemaining;
            }
            tv.setAngleRad(TRANSFORM.get(entity).orientRad + MathUtils.PI / 2).nor().scl(MathUtils.cos(t * 50) * t * t / 5);
            tv2.add(tv);
        }
        Character character = CHARACTER.get(entity);
        if (CHARACTER.get(game.playerEntity).faction.isEnemy(character.faction)) {
            batch.setColor(Color.RED);
        } else {
            batch.setColor(Color.GREEN);
        }
        batch.draw(assets.textures.get("particle"), tv2.x - 2, tv2.y - 2, 4, 4);
        batch.setColor(Color.WHITE);
    }

    public void drawCelestial(Entity entity) {
        determineDrawPosition(entity);
        Celestial celestial = CELESTIAL.get(entity);
        batch.setColor(celestial.radarColor);
        tv.set(BODY.get(entity).dimension);
        tv.x = (float) Math.log(tv.x);
        tv.y = (float) Math.log(tv.y);
        tv.scl(CELESTIAL_SCALE);
        batch.draw(assets.textures.get("particle"), tv2.x - tv.x, tv2.y - tv.y, tv.x * 2, tv.y * 2);
        batch.setColor(Color.WHITE);
    }

    private void determineDrawPosition(Entity entity) {
        float radius = Math.min(bounds.getWidth() / 2 - BORDER, bounds.getHeight() / 2 - BORDER);
        Transform transform = TRANSFORM.get(entity);
        tv.set(transform.location).sub(position);
        float dst = tv.len();
        tv.setLength((1 - 1000 / (dst + 1000)) * radius);
        bounds.getCenter(tv2);
        tv2.add(tv);
    }
}
