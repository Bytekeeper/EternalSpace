package org.bk.desktop;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Array<Polygon> triangles = new Array<Polygon>();
        Array<Face> faces = new Array<Face>(true, 20);
        Array<Array<Face>> faceIterations = new Array<Array<Face>>();
        faces.add(new Face(0, -rnd.nextInt(100) - 50, 0, rnd.nextInt(100) + 50));
        for (int i = 0; i < 7; i++) {
            Face best = null;
            for (int j = 0; j < 3; j++) {
                Face face = faces.random();
                if (best == null || Math.max(face.x1, face.x2) > Math.max(best.x1, best.x2)) {
                    best = face;
                }
            }
            Face face = best;
            int toReplace = faces.indexOf(face, true);
            int dx = face.y2 - face.y1;
            dx *= rnd.nextFloat() * 0.3f + 0.3;
            int dy = face.x1 - face.x2;
            dy *= rnd.nextFloat() * 0.3f + 0.3;
            dx += face.x1 + (face.x2 - face.x1) * (rnd.nextFloat() * 1.2 - 0.1);
            dy += face.y1 + (face.y2 - face.y1) * (rnd.nextFloat() * 1.2 - 0.1);
            Face a = new Face(face.x1, face.y1, dx, dy);
            Face b = new Face(dx, dy, face.x2, face.y2);
            faces.set(toReplace, b);
            faces.insert(toReplace, a);
            Polygon polygon = new Polygon(new int[]{face.x1, dx, face.x2},
                    new int[]{face.y1, dy, face.y2}, 3);
            triangles.add(polygon);
//            for (float t = 1; t > 0.9; t -= 0.02f) {
//                float shade = 1.7f - t * 1.3f;
//                g.setColor(new Color(shade, shade, shade));
//                Polygon smaller = resize(polygon, t);
//                g.fillPolygon(smaller);
//            }
//            g.setColor(Color.GRAY);
//            polygon = new Polygon(new int[]{256 - face.x1, 256 - dx, 256 - face.x2},
//                    new int[]{256 + face.y1, 256 + dy, 256 + face.y2}, 3);
//            for (float t = 1; t > 0.9; t -= 0.02f) {
//                float shade = 1.7f - t * 1.3f;
//                g.setColor(new Color(shade, shade, shade));
//                Polygon smaller = resize(polygon, t);
//                g.fillPolygon(smaller);
//            }
            faceIterations.add(new Array<Face>(faces));
        }

        for (int i = faceIterations.size - 1; i >= 2; i -= 4) {
            Array<Face> facesToProcess = faceIterations.get(i);
            Polygon polygon = polygonOf(facesToProcess, true);
            polygon.translate(256, 256);
            g.setColor(Color.GRAY);
            for (float t = 1; t > 0.9; t -= 0.02f) {
                float shade = 2.3f - t * 1.7f;
                g.setColor(new Color(shade, shade, shade));
                Polygon smaller = resize(polygon, t);
                g.fillPolygon(smaller);
            }
        }
        drawDecals(g, faces, 3, 2, 10);
        drawDecals(g, triangles);

        for (int i = 0; i < 2; i++) {
            Polygon polygon = triangles.get(rnd.nextInt(triangles.size - 4) + 2);
            int[] ctr = center(polygon);
            int a = (int) Vector2.len(polygon.xpoints[1] - polygon.xpoints[0], polygon.ypoints[1] - polygon.ypoints[0]);
            int b = (int) Vector2.len(polygon.xpoints[2] - polygon.xpoints[0], polygon.ypoints[2] - polygon.ypoints[0]);
            int c = (int) Vector2.len(polygon.xpoints[1] - polygon.xpoints[2], polygon.ypoints[1] - polygon.ypoints[2]);
            int s = (a + b + c) / 2;
            int r = (int) Math.sqrt((s - a) * (s - b) * (s - c) / s);
            drawDecalCircle(g, ctr[0], ctr[1], r / 2);
        }

        g.setColor(new Color(0.8f, 0.8f, 0.8f));
        g.fillOval(256 - 20, 256 - 50, 40, 100);
        g.setColor(Color.GRAY);
        g.drawOval(256 - 20, 256 - 50, 40, 100);
        for (int i = 15; i > 10; i--) {
            float shade = (15 - i) / 30f + 0.3f;
            g.setColor(new Color(shade, shade, Math.min(1, shade * 2)));
            g.fillOval(256 - i, 256 - i * 3, i * 2, i * 6);
        }
        g.setColor(new Color(0.9f, 0.9f, 1));
        g.fillOval(256 - 6, 256 - 30, 12, 6);;
        ImageIO.write(image, "png", new File("target.png"));
    }

    private static void drawDecals(Graphics2D g, Array<Polygon> polygons) {
        int pindex = rnd.nextInt(polygons.size - 4) + 2;
        Polygon polygon = polygons.get(pindex);
        int x[] = polygon.xpoints;
        int y[] = polygon.ypoints;
        int x1 = x[0] * 8 / 10;
        int y1 = y[0] * 8 / 10;
        int x2 = x[1] * 8 / 10;
        int y2 = y[1] * 8 / 10;
        int x3 = x[2] * 8 / 10;
        int y3 = y[2] * 8 / 10;
        decalLine(g, x1, y1, x3, y3);
        decalLine(g, x1, y1, x2, y2);
    }

    private static void drawDecals(Graphics2D g, Array<Face> faces, int length, int decalAmount, int scale) {
        int decal = rnd.nextInt(faces.size - length - 2) + 1;
        for (int j = 0; j < length; j++) {
            Face f = faces.get(decal + j);
            for (int k = scale - decalAmount; k < scale; k++) {
                int x1 = f.x1 * k / scale;
                int y1 = f.y1 * k / scale;
                int x2 = f.x2 * k / scale;
                int y2 = f.y2 * k / scale;
                decalLine(g, x1, y1, x2, y2);
            }
        }
    }

    private static void decalLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setColor(Color.WHITE);
        g.drawLine(x1 + 257, y1 + 256,
                x2 + 257, y2 + 256);
        g.drawLine(255 - x1, y1 + 256,
                255 - x2, y2 + 256);
        g.setColor(Color.DARK_GRAY);
        g.drawLine(256 + x1, y1 + 256,
                256 + x2, y2 + 256);
        g.drawLine(256 - x1, y1 + 256,
                256 - x2, y2 + 256);
    }

    private static void drawDecalCircle(Graphics2D g, int x, int y, int r) {
        g.setColor(Color.DARK_GRAY);
        g.drawOval(256 + x - r, 256 + y - r, r * 2, r * 2);
        g.drawOval(256 - x - r, 256 + y - r, r * 2, r * 2);
    }

    public static Polygon resize(Polygon polygon, float scale) {
        int[] c = center(polygon);
        int[] xpoints = new int[polygon.npoints];
        int[] ypoints = new int[polygon.npoints];
        for (int i = 0; i < polygon.npoints; i++) {
            xpoints[i] = (int) ((polygon.xpoints[i] - c[0]) * scale + c[0]);
            ypoints[i] = (int) ((polygon.ypoints[i] - c[1]) * scale + c[1]);
        }
        return new Polygon(xpoints, ypoints, polygon.npoints);
    }

    private static int[] center(Polygon polygon) {
        int c[] = new int[2];
        for (int i = 0; i < polygon.npoints; i++) {
            c[0] += polygon.xpoints[i];
            c[1] += polygon.ypoints[i];
        }
        c[0] /= polygon.npoints;
        c[1] /= polygon.npoints;
        return c;
    }

    public static Polygon polygonOf(Array<Face> faces, boolean addMirrored) {
        int npoints = addMirrored ? faces.size * 2 + 1: faces.size + 1;
        int x[] = new int[npoints];
        int y[] = new int[npoints];
        x[0] = faces.get(0).x1;
        y[0] = faces.get(0).y1;
        for (int i = 0; i < faces.size; i++) {
            x[i + 1] = faces.get(i).x2;
            y[i + 1] = faces.get(i).y2;
        }
        if (addMirrored) {
            for (int i = 0; i < faces.size; i++) {
                x[faces.size + i + 1] = -x[faces.size - i];
                y[faces.size + i + 1] = y[faces.size - i];
            }
        }
        return new Polygon(x, y, npoints);
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

        @Override
        public String toString() {
            return "Face{" +
                    "x1=" + x1 +
                    ", y1=" + y1 +
                    ", x2=" + x2 +
                    ", y2=" + y2 +
                    '}';
        }
    }
}
