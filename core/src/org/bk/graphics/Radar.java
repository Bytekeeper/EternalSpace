package org.bk.graphics;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
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

    }

    public void drawShip(Entity entity) {
        float radius = Math.min(bounds.getWidth() / 2, bounds.getHeight() / 2);
        Transform transform = TRANSFORM.get(entity);
        tv.set(transform.location).sub(position);
        float dst = tv.len();
        tv.setLength((1 - 1000 / (dst + 1000)) * radius);
        bounds.getCenter(tv2);
        tv2.add(tv);
        batch.draw(assets.bg_star, tv2.x - 1, tv2.y - 1, 5, 5);
    }
}
