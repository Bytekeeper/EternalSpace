package org.bk.graphics;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import org.bk.Assets;
import org.bk.Game;
import org.bk.component.Transform;

import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 16.10.2016.
 */
public class Radar {
    private static final float BORDER = 20;
    public final Rectangle bounds = new Rectangle();
    public final Vector2 position = new Vector2();
    private final Assets assets;
    private SpriteBatch batch;
    private final Vector2 tv = new Vector2();
    private final Vector2 tv2 = new Vector2();

    public Radar(Game game) {
        this.batch = game.uiBatch;
        assets = game.assets;
    }

    public void drawBackground() {
        batch.draw(assets.ui_radar, bounds.x, bounds.y, bounds.getWidth(), bounds.getHeight());
    }

    public void drawShip(Entity entity) {
        determineDrawPosition(entity);
        batch.draw(assets.bg_star, tv2.x - 2, tv2.y - 2, 4, 4);
    }

    public void drawPlanet(Entity entity) {
        determineDrawPosition(entity);
        batch.setColor(Color.OLIVE);
        batch.draw(assets.bg_star, tv2.x - 4, tv2.y - 4, 8, 8);
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
