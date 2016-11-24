package org.bk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by dante on 22.11.2016.
 */
public class BackgroundStars {
    public static final int STAR_BORDER = 200;
    public static final int STAR_BORDER2 = STAR_BORDER * 2;
    private Array<Star> stars = new Array<Star>();
    private float density = 1 / 600f;
    private int width;
    private int height;
    private int starAmount;
    private final Viewport viewport;
    private final TextureRegion starRegion;
    private Vector2 tv = new Vector2();
    private RandomXS128 rnd = new RandomXS128();

    public BackgroundStars(Viewport viewport, TextureRegion starRegion) {
        this.viewport = viewport;
        this.starRegion = starRegion;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        starAmount = (int) (width * height * density);
    }

    public void draw(SpriteBatch batch) {
        if (stars.size != starAmount) {
            Pools.freeAll(stars);
            stars.clear();
            for (int i = 0; i < starAmount; i++) {
                Star star = Pools.obtain(Star.class);
                tv.set(rnd.nextFloat() * (width + STAR_BORDER2) - STAR_BORDER,
                        rnd.nextFloat() * (height + STAR_BORDER2) - STAR_BORDER);
                viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = brightness();
                stars.add(star);
            }
        }
        for (Star star : stars) {
            tv.set(star.position);
            viewport.project(tv);
            if (tv.x < -STAR_BORDER || tv.x > width + STAR_BORDER) {
                tv.x += Math.signum(width / 2 - tv.x) * (width + STAR_BORDER * (1 + rnd.nextFloat()));
                tv.y = rnd.nextFloat() * (Gdx.graphics.getHeight() + STAR_BORDER2) - STAR_BORDER;
                viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = brightness();
            } else if (tv.y < -STAR_BORDER || tv.y > height + STAR_BORDER) {
                tv.x = rnd.nextFloat() * (width + STAR_BORDER2) - STAR_BORDER;
                tv.y += Math.signum(Gdx.graphics.getHeight() / 2 - tv.y) * (Gdx.graphics.getHeight() + STAR_BORDER * (1 + rnd.nextFloat()));
                tv.y = Gdx.graphics.getHeight() - tv.y;
                viewport.unproject(tv);
                star.position.set(tv);
                star.brightness = brightness();
            }
            batch.setColor(star.brightness, star.brightness, star.brightness, 1);
            batch.draw(starRegion, star.position.x, star.position.y, 3, 3);
        }
        batch.setColor(Color.WHITE);
    }

    private float brightness() {
        return rnd.nextFloat() * 0.5f + 0.2f;
    }

    public static class Star {
        final Vector2 position = new Vector2();
        float brightness;
    }
}
