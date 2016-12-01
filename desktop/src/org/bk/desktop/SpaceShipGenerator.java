package org.bk.desktop;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
        int baseHeight = 300;
        int variableHeight = 100;
        int baseTriangleHeight = 30;
        int variableTriangleHeight = 60;
        int numTriangles = 12;
        int wideningSteps = 2;
        faces.add(new Face(0, -rnd.nextInt(variableHeight / 2) - baseHeight / 2, 0, rnd.nextInt(variableHeight / 2) + baseHeight / 2));
        for (int i = 0; i < numTriangles; i++) {
            Face best = null;
            for (int j = 0; j < wideningSteps; j++) {
                Face face = faces.random();
                if (best == null || Math.max(face.x1, face.x2) > Math.max(best.x1, best.x2)) {
                    best = face;
                }
            }
            Face face = best;
            int toReplace = faces.indexOf(face, true);
            Vector2 nextCorner = new Vector2(face.x2 - face.x1, face.y2 - face.y1);
            nextCorner.rotate90(-1).setLength(rnd.nextFloat() * variableTriangleHeight + baseTriangleHeight);
            nextCorner.add(face.x1 * 0.5f, face.y1 * 0.5f);
            nextCorner.add(face.x2 * 0.5f, face.y2 * 0.5f);
//            int dx = face.y2 - face.y1;
//            dx *= rnd.nextFloat() * 0.3f + 0.3;
//            int dy = face.x1 - face.x2;
//            dy *= rnd.nextFloat() * 0.3f + 0.3;
//            dx += face.x1 + (face.x2 - face.x1) * (rnd.nextFloat() * 1.2 - 0.1);
//            dy += face.y1 + (face.y2 - face.y1) * (rnd.nextFloat() * 1.2 - 0.1);
            int dx = (int) nextCorner.x;
            int dy = (int) nextCorner.y;
            Face a = new Face(face.x1, face.y1, dx, dy);
            Face b = new Face(dx, dy, face.x2, face.y2);
            faces.set(toReplace, b);
            faces.insert(toReplace, a);
            Polygon polygon = new Polygon(new int[]{face.x1, dx, face.x2},
                    new int[]{face.y1, dy, face.y2}, 3);
            triangles.add(polygon);

//            Polygon p = new Polygon(polygon.xpoints, polygon.ypoints, 3);
//            p.translate(256, 256);
//            g.drawPolygon(p);
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

        drawGuns(g, triangles);

        float baseShade = 2.1f;
        for (int i = faceIterations.size - 1; i >= 2; i -= 3) {
            Array<Face> facesToProcess = faceIterations.get(i);
            Polygon polygon = polygonOf(facesToProcess, true);
            polygon.translate(256, 256);
            g.setColor(Color.GRAY);
            for (float t = 1; t > 0.9; t -= 0.005f) {
                float shade = baseShade - t * 1.7f;
                g.setColor(new Color(shade, shade, shade));
                Polygon smaller = resize(polygon, t);
                g.fillPolygon(smaller);
            }
            baseShade += 0.05f;
        }
        drawHBars(g, triangles);

        drawDecals(g, triangles);
        drawDecals(g, triangles);
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
        g.fillOval(256 - 6, 256 - 30, 12, 6);
        ImageIO.write(image, "png", new File("target.png"));
    }

    private static void drawGuns(Graphics2D g, Array<Polygon> triangles) {
        for (int i = 0; i < 30; i++) {
            Polygon base = triangles.get(rnd.nextInt(triangles.size - 2) + 2);
            int best = -1;
            for (int j = 0; j < 3; j++) {
                if (base.xpoints[j] > 10 && (best < 0 || base.ypoints[j] < base.ypoints[best])) {
                    best = j;
                }
            }
            if (best < 0) {
                continue;
            }
            int x = (int) (base.xpoints[best] * 0.9);
            int y = base.ypoints[best] + 20;
//            gunPlaceholder.translate(0, -10);
            boolean collides = false;
            for (int j = 0; j < triangles.size; j++) {
                if (triangles.get(j) == base) {
                    continue;
                }
                com.badlogic.gdx.math.Polygon poly2 = toGdxPoly(triangles.get(j));
                if (Intersector.intersectSegmentPolygon(new Vector2(x, y), new Vector2(x, y - 100), poly2)) {
                    collides = true;
                    break;
                }
            }
            if (!collides) {
                g.setColor(Color.GRAY);
                g.fillRect(x + 248, y + 226, 10, 50);
                g.fillRect(254 - x, y + 226, 10, 50);
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x + 251, y + 206, 5, 67);
                g.fillRect(256 - x, y + 206, 5, 67);
                g.setColor(Color.DARK_GRAY);
                for (int j = 0; j < 5 ; j++) {
                    g.fillRect(x + 248, y + 226 + j * 10, 10, 5);
                    g.fillRect(254 - x, y + 226 + j * 10, 10, 5);
                }
                break;
            }
        }
    }

    private static com.badlogic.gdx.math.Polygon toGdxPoly(Polygon base) {
        return new com.badlogic.gdx.math.Polygon(new float[]{base.xpoints[0], base.ypoints[0],
                base.xpoints[1], base.ypoints[1], base.xpoints[2], base.ypoints[2]});
    }

    private static Polygon toJavaPoly(com.badlogic.gdx.math.Polygon polygon) {
        float[] v = polygon.getTransformedVertices();
        return new Polygon(new int[] {(int) v[0], (int) v[2], (int) v[4]},
                new int[] {(int) v[1], (int) v[2], (int) v[3]}, 3);
    }


    private static void drawHBars(Graphics2D g, Array<Polygon> triangles) {
        Polygon best = null;
        double bestArea = 0;
        for (int i = 0; i < 8; i++) {
            Polygon a = triangles.random();
            double pa = area(a);
            {
                if (pa > bestArea) {
                    best = a;
                    bestArea = pa;
                }
            }
        }
        if (best != null) {
            Polygon resized = resize(best, 0.8f);
            Vector2 segment = new Vector2(resized.xpoints[2] - resized.xpoints[1], resized.ypoints[2] - resized.ypoints[1]);
            Vector2 dir1 = new Vector2(resized.xpoints[0] - resized.xpoints[1], resized.ypoints[0] - resized.ypoints[1]).nor();
            Vector2 dir2 = new Vector2(segment).rotate90(1).nor();
            Vector2 pos = new Vector2(resized.xpoints[1], resized.ypoints[1]);
            double bheight = bheight(resized);
            resized.translate(256, 256);
//            g.setColor(Color.YELLOW);
//            g.drawPolygon(resized);

            for (int i = 0; i < 3; i++) {
                segment.scl((float) ((bheight - 20) / bheight));
                pos.mulAdd(dir1, 20);
                if (bheight < 10) {
                    break;
                }
                int x[] = new int[]{(int) pos.x, (int) (pos.x + segment.x), (int) (pos.x + segment.x + dir2.x * 10), (int) (pos.x + dir2.x * 10)};
                int y[] = new int[]{(int) pos.y, (int) (pos.y + segment.y), (int) (pos.y + segment.y + dir2.y * 10), (int) (pos.y + dir2.y * 10)};
                Polygon toDraw = new Polygon(x, y, 4);
                toDraw.translate(256, 256);
                g.setColor(Color.RED.darker());
                g.fillPolygon(toDraw);
                for (int j = 0; j < 4; j++) {
                    x[j] = - x[j];
                }
                toDraw = new Polygon(x, y, 4);
                toDraw.translate(256, 256);
                g.fillPolygon(toDraw);
            }
        }
    }

    private static double bheight(Polygon p) {
        int a = (int) Vector2.len(p.xpoints[1] - p.xpoints[0], p.ypoints[1] - p.ypoints[0]);
        int a2 = a * a;
        int b = (int) Vector2.len(p.xpoints[2] - p.xpoints[0], p.ypoints[2] - p.ypoints[0]);
        int b2 = b * b;
        int c = (int) Vector2.len(p.xpoints[1] - p.xpoints[2], p.ypoints[1] - p.ypoints[2]);
        int c2 = c * c;
        return Math.sqrt(2 * (a2 * b2 + b2 * c2 + c2 * a2) - (a2 * a2 + b2 * b2 + c2 * c2)) / (2 * b);
    }

    private static double area(Polygon p) {
        int a = (int) Vector2.len(p.xpoints[1] - p.xpoints[0], p.ypoints[1] - p.ypoints[0]);
        int b = (int) Vector2.len(p.xpoints[2] - p.xpoints[0], p.ypoints[2] - p.ypoints[0]);
        int c = (int) Vector2.len(p.xpoints[1] - p.xpoints[2], p.ypoints[1] - p.ypoints[2]);
        int s = (a + b + c) / 2;
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }

    private static void drawDecals(Graphics2D g, Array<Polygon> polygons) {
        int pindex = rnd.nextInt(polygons.size);
        Polygon polygon = resize(polygons.get(pindex), 0.8f);
        int x[] = polygon.xpoints;
        int y[] = polygon.ypoints;
        decalLine(g, x[0], y[0], x[2], y[2]);
        decalLine(g, x[0], y[0], x[1], y[1]);
    }

    private static void decalLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setColor(Color.WHITE);
        g.drawLine(x1 + 255, y1 + 256,
                x2 + 255, y2 + 256);
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
        int npoints = addMirrored ? faces.size * 2 + 1 : faces.size + 1;
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
