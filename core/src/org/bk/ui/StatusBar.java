package org.bk.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Created by dante on 03.11.2016.
 */
public class StatusBar extends Widget {
    private final NinePatchDrawable background;
    private final NinePatchDrawable statusBar;
    private float factor;

    public StatusBar(NinePatchDrawable background, NinePatchDrawable statusBar) {
        this.background = background;
        this.statusBar = statusBar;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        background.draw(batch, getX(), getY(), getWidth() * getScaleX(), getHeight() * getScaleY());
        if (factor > 0) {
            statusBar.draw(batch, getX(), getY(), factor * getWidth() * getScaleX(), getHeight() * getScaleY());
        }
    }
}
