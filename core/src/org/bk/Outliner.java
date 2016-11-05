package org.bk;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 05.11.2016.
 */
public class Outliner {
    private Color tc = new Color();

    public Polygon determineOutlineOf(TextureRegion region) {
        TextureData textureData = region.getTexture().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        if (textureData.getFormat() != Pixmap.Format.RGBA8888) {
            throw new IllegalStateException("Unsupported format");
        }
        Pixmap pixmap = textureData.consumePixmap();

        boolean[][] mark = new boolean[region.getRegionHeight() + 2][region.getRegionWidth() + 2];
        boolean[][] outline = new boolean[region.getRegionHeight()][region.getRegionWidth()];

        fill(mark, outline, pixmap, region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight());

        if (textureData.disposePixmap()) {
            pixmap.dispose();
        }
        Polygon result = null;
        boolean[][] visited = new boolean[region.getRegionHeight()][region.getRegionWidth()];
        for (int y = 0; y < region.getRegionHeight(); y++) {
            for (int x = 0; x < region.getRegionWidth(); x++) {
                float[] vertices = poly(visited, outline, x, y);
                if (vertices != null) {
                    float[] verts = douglasPeucker(vertices, 1f);
                    result = new Polygon(verts);
                }
            }
        }

        return result;
    }

    private float[] poly(boolean[][] visited, boolean[][] outline, int x, int y) {
        return poly(visited, outline, x, y, x, y, x, y, 0);
    }

    private float[] poly(boolean[][] visited, boolean[][] outline, int x, int y, int curX, int curY, int lastX, int lastY, int length) {
        if (curX == x && curY == y && lastX != curX && lastY != curY) {
            return new float[length * 2];
        }
        if (curX < 0 || curY < 0 || curX >= outline[0].length || curY >= outline.length || visited[curY][curX] || !outline[curY][curX]) {
            return null;
        }
        visited[curY][curX] = true;
        float result[] = null;
        for (int i = -1; i <= 1; i++) {
            for (int j = 1; j >= -1; j--) {
                if (curX + j != lastX || curY + i != lastY ) {
                    float[] tmp = poly(visited, outline, x, y, curX + j, curY + i, curX, curY, length + 1);
                    if (tmp != null && (result == null || tmp.length > result.length )) {
                        tmp[length * 2] = curX;
                        tmp[length * 2 + 1] = curY;
                        result = tmp;
                    }
                }
            }
        }
        return result;
    }

    private void fill(boolean[][] mark, boolean[][] outline, Pixmap pixmap, int sX, int sY, int w, int h) {
        Array<int[]> points = new Array<int[]>();
        points.add(new int[] {-1, -1});
        while (points.size > 0) {
            int[] next = points.pop();
            int x = next[0];
            int y = next[1];
            if (x < -1 || x > w || y < -1 || y > h) {
                continue;
            }
            if (mark[y + 1][x + 1]) {
                continue;
            }
            mark[y + 1][x + 1] = true;
            if (x >= 0 && x < w && y >= 0 && y < h) {
                tc.set(pixmap.getPixel(x + sX, y + sY));
                if (tc.a > 0.1) {
                    outline[y][x] = true;
                    continue;
                }
            }
            points.add(new int[] {x - 1, y});
            points.add(new int[] {x + 1, y});
            points.add(new int[] {x, y - 1});
            points.add(new int[] {x, y + 1});
        }
    }

    public float[] douglasPeucker(float vertices[], float eps) {
        boolean[] marked = new boolean[vertices.length / 2];
        int vcount = douglasPeucker0(marked, vertices, eps, 0, vertices.length / 2 - 1);
        float[] result = new float[vcount * 2];
        int index = 0;
        for (int i = 0; i < marked.length; i++) {
            if (marked[i]) {
                result[index++] = vertices[i * 2];
                result[index++] = vertices[i * 2 + 1];
            }
        }
        return result;
    }

    private static int douglasPeucker0(boolean[] marked, float[] vertices, float eps, int start, int end) {
        // Find the point with the maximum distance
        float dmax = 0;
        int index = 0;
        for (int i = start + 1; i < end; i++) {
            float d = Intersector.distanceLinePoint(vertices[start * 2], vertices[start * 2 + 1],
                    vertices[end * 2], vertices[end * 2 + 1], vertices[i * 2], vertices[i * 2 + 1]);
            if (d > dmax) {
                dmax = d;
                index = i;
            }
        }
        if (dmax > eps) {
            return douglasPeucker0(marked, vertices, eps, start, index)
                    + douglasPeucker0(marked, vertices, eps, index, end);
        } else {
            int sum = 0;
            if (!marked[start]) {
                sum++;
                marked[start] = true;
            }
            if (!marked[end]) {
                sum++;
                marked[end] = true;
            }
            return sum;
        }
    }
}
