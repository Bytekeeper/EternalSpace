package org.bk.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 06.11.2016.
 */
public class Shape extends Widget {
    private final ShapeRenderer shapeRenderer;
    private Array<float[]> shape;
    private float minX, minY, maxX, maxY;
    private final Matrix4 tm = new Matrix4();
    private float rotation = 90;

    public Shape(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public void setShape(Array<float[]> shape) {
        if (this.shape == shape) {
            return;
        }
        this.shape = shape;
        if (shape == null) {
            return;
        }
        minX = maxX = shape.first()[0];
        minY = maxY = shape.first()[1];
        for (float f[]: shape) {
            for (int i = 0; i < f.length; i += 2) {
                minX = Math.min(minX, f[i]);
                maxX = Math.max(maxX, f[i]);
                minY = Math.min(minY, f[i + 1]);
                maxY = Math.max(maxY, f[i + 1]);
            }
        }
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shape == null) {
            return;
        }
        super.draw(batch, parentAlpha);
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        tm.set(shapeRenderer.getTransformMatrix());
        shapeRenderer.translate(-minX + getX(), -minY + getY(), 0);
        shapeRenderer.rotate(0, 0, 1, rotation);
        for (float[] f: shape) {
            shapeRenderer.polygon(f);
        }
        shapeRenderer.setTransformMatrix(tm);
        shapeRenderer.end();
        batch.begin();
    }

    @Override
    public float getPrefWidth() {
        return maxX - minX;
    }

    @Override
    public float getPrefHeight() {
        return maxY - minY;
    }
}
