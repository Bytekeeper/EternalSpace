package org.bk.desktop;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dante on 17.11.2016.
 */
public class SpaceShipGenerator {
    private static RandomXS128 rnd = new RandomXS128();
    public static final int CX = 256;
    public static final int CY = 256;

    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.GRAY);

        Array<Polygon> polygons = new Array<Polygon>();
        Array<Face> faces = new Array<Face>(false, 10);
        faces.add(new Face(0, -rnd.nextInt(150), 0, rnd.nextInt(150)));
        for (int i = 0; i < 7; i++) {
            Face best = null;
            for (int j = 0; j < 3; j++) {
                Face face = faces.random();
                if (best == null || Math.max(face.x1, face.x2) > Math.max(best.x1, best.x2)) {
                    best = face;
                }
            }
            Face face = best;
            faces.removeValue(face, true);
            int dx = face.y2 - face.y1;
            dx *= rnd.nextFloat() * 0.3f + 0.3;
            int dy = face.x1 - face.x2;
            dy *= rnd.nextFloat() * 0.3f + 0.3;
            dx += face.x1 + (face.x2 - face.x1) * (rnd.nextFloat() * 1.2 - 0.1);
            dy += face.y1 + (face.y2 - face.y1) * (rnd.nextFloat() * 1.2 - 0.1);
            Face a = new Face(face.x1, face.y1, dx, dy);
            Face b = new Face(dx, dy, face.x2, face.y2);
            faces.addAll(a, b);
            Polygon polygon = new Polygon(new int[]{256 + face.x1, 256 + dx, 256 + face.x2},
                    new int[]{256 + face.y1, 256 + dy, 256 + face.y2}, 3);
            polygons.add(polygon);
            for (float t = 1; t > 0.7; t -= 0.05f) {
                float shade = 1.7f - t * 1.3f;
                g.setColor(new Color(shade, shade, shade));
                Polygon smaller = resize(polygon, t);
                g.fillPolygon(smaller);
            }
            g.setColor(Color.GRAY);
            polygon = new Polygon(new int[]{256 - face.x1, 256 - dx, 256 - face.x2},
                    new int[]{256 + face.y1, 256 + dy, 256 + face.y2}, 3);
            for (float t = 1; t > 0.7; t -= 0.05f) {
                float shade = 1.7f - t * 1.3f;
                g.setColor(new Color(shade, shade, shade));
                Polygon smaller = resize(polygon, t);
                g.fillPolygon(smaller);
            }
        }

        ImageIO.write(image, "png", new File("target.png"));
    }

    public static Polygon resize(Polygon polygon, float scale) {
        int cx = (polygon.xpoints[0] + polygon.xpoints[1] + polygon.xpoints[2]) / 3;
        int cy = (polygon.ypoints[0] + polygon.ypoints[1] + polygon.ypoints[2]) / 3;
        int[] xpoints = new int[polygon.npoints];
        int[] ypoints = new int[polygon.npoints];
        for (int i = 0; i < polygon.npoints; i++) {
            xpoints[i] = (int) ((polygon.xpoints[i] - cx) * scale + cx);
            ypoints[i] = (int) ((polygon.ypoints[i] - cy) * scale + cy);
        }
        return new Polygon(xpoints, ypoints, polygon.npoints);
    }

    private static class Face {
        public int x1, y1;
        public int x2, y2;

        public Face(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public float len2() {
            return (y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1);
        }
    }
}
