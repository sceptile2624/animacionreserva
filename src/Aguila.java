import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Aguila extends JPanel implements Runnable {

    private BufferedImage canvas;
    private int cloudX1, cloudX2;
    private Leaf[] leaves;
    private int eagleX, eagleY;
    private final int cloudSpeed = 2;

    // Coordenadas del águila
    private final int[][] eagleBeak = {
            {2, 8}, {1, 11}, {1, 12}, {2, 15}, {2, 16}, {4, 15}, {6, 13}, {3, 11}, {2, 10}
    };
    private final int[][] eagleBody = {
            {3, 18}, {7, 21}, {10, 21}, {15, 18}, {16, 18}, {16, 15}, {15, 15}, {17, 12}, {16, 12},
            {18, 8}, {17, 8}, {19, 5}, {18, 3}, {16, 5}, {16, 3}, {13, 5}, {13, 2}, {10, 5}, {10, 1},
            {7, 4}, {0, 0}, {4, 9}, {3, 10}
    };
    private final int[][] eagleEye = {
            {6, 16}, {7, 17}, {7, 16}, {9, 18}
    };

    // Coordenadas de la hoja
    private final int[] xPoints = {1, 5, 4, 9, 7, 8, 5, 5, 3, 0, -2, -4, -3, -4, -3, -5, -5, -8, -7, -9, -4, -5, 0, 2, 2, 1};
    private final int[] yPoints = {-3, -4, -3, 1, 2, 5, 4, 5, 4, 10, 7, 8, 3, 8, 3, 6, 4, 5, 2, 1, -3, -4, -3, -7, -6, -3};

    public Aguila() {
        setPreferredSize(new Dimension(1280, 720));
        canvas = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        cloudX1 = 0;
        cloudX2 = 640;
        eagleX = 640; // Centrado horizontalmente
        eagleY = 540; // Centrado verticalmente, con el pecho tocando el fondo del marco
        initializeLeaves();
        new Thread(this).start();
    }

    // Inicializa las hojas con posiciones y rotaciones aleatorias
    private void initializeLeaves() {
        leaves = new Leaf[10];
        Random random = new Random();
        for (int i = 0; i < leaves.length; i++) {
            int leafX = random.nextInt(1280);
            int leafY = random.nextInt(720);
            int leafSize = 20;
            int leafRotation = random.nextInt(360); // Rotación aleatoria de la hoja
            leaves[i] = new Leaf(leafX, leafY, leafSize, leafRotation);
        }
    }

    // Actualiza la posición de las hojas
    private void updateLeaves() {
        for (Leaf leaf : leaves) {
            leaf.y += 1; // Velocidad de caída más lenta
            if (leaf.y % 10 == 0) { // Reduce el tamaño cada 10 píxeles de caída
                leaf.size = Math.max(1, leaf.size - 1); // Disminuye el tamaño hasta un mínimo de 1
            }
            if (leaf.y > 720) {
                leaf.y = 0;
                leaf.size = 20;
                leaf.x = new Random().nextInt(1280);
                leaf.rotation = new Random().nextInt(360); // Nueva rotación aleatoria
            }
        }
    }

    // Actualiza la posición de las nubes
    private void updateClouds() {
        cloudX1 += cloudSpeed;
        cloudX2 += cloudSpeed;
        if (cloudX1 > 1280) {
            cloudX1 = -200;
        }
        if (cloudX2 > 1280) {
            cloudX2 = -200;
        }
    }

    // Pone un pixel en la imagen
    private void putPixel(int x, int y, Color color) {
        if (x >= 0 && x < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
            canvas.setRGB(x, y, color.getRGB());
        }
    }

    // Dibuja una línea usando el algoritmo DDA
    private void drawLineDDA(int x1, int y1, int x2, int y2, Color color) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        float xIncrement = dx / (float) steps;
        float yIncrement = dy / (float) steps;
        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            putPixel(Math.round(x), Math.round(y), color);
            x += xIncrement;
            y += yIncrement;
        }
    }

    // Dibuja una línea usando el algoritmo de Bresenham
    private void drawLineBresenham(int x1, int y1, int x2, int y2, Color color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            putPixel(x1, y1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    // Dibuja un círculo usando el algoritmo de Bresenham
    private void drawCircleBresenham(int centerX, int centerY, int radius, Color color) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;
        fillCircle(centerX, centerY, x, y, color);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            fillCircle(centerX, centerY, x, y, color);
        }
    }

    // Rellena un círculo
    private void fillCircle(int centerX, int centerY, int x, int y, Color color) {
        for (int i = centerX - x; i <= centerX + x; i++) {
            putPixel(i, centerY + y, color);
            putPixel(i, centerY - y, color);
        }
        for (int i = centerX - y; i <= centerX + y; i++) {
            putPixel(i, centerY + x, color);
            putPixel(i, centerY - x, color);
        }
    }

    // Dibuja y rellena un polígono
    private void drawPolygon(int[][] points, int offsetX, int offsetY, int scale, Color color) {
        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];

        for (int i = 0; i < points.length; i++) {
            xPoints[i] = offsetX + points[i][0] * scale;
            yPoints[i] = offsetY - points[i][1] * scale;
        }

        for (int i = 0; i < points.length - 1; i++) {
            drawLineBresenham(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], color);
        }
        drawLineBresenham(xPoints[points.length - 1], yPoints[points.length - 1], xPoints[0], yPoints[0], color);
        fillPolygon(xPoints, yPoints, color);
    }

    // Rellena un polígono
    private void fillPolygon(int[] xPoints, int[] yPoints, Color color) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = 0; i < xPoints.length; i++) {
            if (xPoints[i] < minX) minX = xPoints[i];
            if (xPoints[i] > maxX) maxX = xPoints[i];
            if (yPoints[i] < minY) minY = yPoints[i];
            if (yPoints[i] > maxY) maxY = yPoints[i];
        }

        for (int y = minY; y <= maxY; y++) {
            boolean inside = false;
            for (int x = minX; x <= maxX; x++) {
                int intersections = 0;
                for (int i = 0; i < xPoints.length - 1; i++) {
                    if (((yPoints[i] > y) != (yPoints[i + 1] > y)) &&
                            (x < (xPoints[i + 1] - xPoints[i]) * (y - yPoints[i]) / (yPoints[i + 1] - yPoints[i]) + xPoints[i])) {
                        intersections++;
                    }
                }
                if (intersections % 2 != 0) {
                    inside = !inside;
                }
                if (inside) {
                    putPixel(x, y, color);
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawScene();
        g.drawImage(canvas, 0, 0, null);
    }

    private void drawScene() {
        // Clear the canvas
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                putPixel(x, y, Color.CYAN);
            }
        }

        // Draw clouds with superimposed circles
        drawCloud(cloudX1, 50);
        drawCloud(cloudX2, 100);

        // Draw the eagle
        drawPolygon(eagleBody, eagleX, eagleY, 20, new Color(139, 69, 19));
        drawPolygon(eagleBeak, eagleX, eagleY, 20, Color.YELLOW);
        drawPolygon(eagleEye, eagleX, eagleY, 20, Color.BLACK);

        // Draw the falling leaves
        for (Leaf leaf : leaves) {
            drawRotatedLeaf(leaf);
        }
    }

    private void drawCloud(int x, int y) {
        drawCircleBresenham(x + 50, y, 50, Color.WHITE);
        drawCircleBresenham(x + 100, y - 20, 50, Color.WHITE);
        drawCircleBresenham(x + 150, y, 50, Color.WHITE);
    }

    private void drawRotatedLeaf(Leaf leaf) {
        int[] rotatedXPoints = new int[xPoints.length];
        int[] rotatedYPoints = new int[yPoints.length];
        double angle = Math.toRadians(leaf.rotation);
        for (int i = 0; i < xPoints.length; i++) {
            rotatedXPoints[i] = leaf.x + (int) (xPoints[i] * leaf.size * Math.cos(angle) - yPoints[i] * leaf.size * Math.sin(angle));
            rotatedYPoints[i] = leaf.y + (int) (xPoints[i] * leaf.size * Math.sin(angle) + yPoints[i] * leaf.size * Math.cos(angle));
        }
        for (int i = 0; i < rotatedXPoints.length - 1; i++) {
            drawLineBresenham(rotatedXPoints[i], rotatedYPoints[i], rotatedXPoints[i + 1], rotatedYPoints[i + 1], new Color(218, 165, 32));
        }
        drawLineBresenham(rotatedXPoints[rotatedXPoints.length - 1], rotatedYPoints[rotatedXPoints.length - 1],
                rotatedXPoints[0], rotatedYPoints[0], new Color(218, 165, 32));
        fillLeaf(rotatedXPoints, rotatedYPoints, new Color(218, 165, 32));
    }

    private void fillLeaf(int[] xPoints, int[] yPoints, Color color) {
        int minX = xPoints[0];
        int maxX = xPoints[0];
        int minY = yPoints[0];
        int maxY = yPoints[0];
        for (int i = 1; i < xPoints.length; i++) {
            if (xPoints[i] < minX) minX = xPoints[i];
            if (xPoints[i] > maxX) maxX = xPoints[i];
            if (yPoints[i] < minY) minY = yPoints[i];
            if (yPoints[i] > maxY) maxY = yPoints[i];
        }

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInPolygon(x, y, xPoints, yPoints)) {
                    putPixel(x, y, color);
                }
            }
        }
    }

    private boolean isPointInPolygon(int x, int y, int[] xPoints, int[] yPoints) {
        boolean result = false;
        int j = xPoints.length - 1;
        for (int i = 0; i < xPoints.length; i++) {
            if ((yPoints[i] > y) != (yPoints[j] > y) &&
                    (x < (xPoints[j] - xPoints[i]) * (y - yPoints[i]) / (yPoints[j] - yPoints[i]) + xPoints[i])) {
                result = !result;
            }
            j = i;
        }
        return result;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (true) {
            updateLeaves();
            updateClouds();
            repaint();
            if (System.currentTimeMillis() - startTime >= 10000) { // Cambia a FinalScene después de 10 segundos
                switchToFinalScene();
                break;
            }
            try {
                Thread.sleep(50); // Velocidad de actualización más rápida
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void switchToFinalScene() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new FinalScene());
        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fin del recorrido: camino");
        Aguila aguila = new Aguila();
        frame.add(aguila);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        new Thread(aguila).start();
    }

    class Leaf {
        int x, y, size, rotation;

        Leaf(int x, int y, int size, int rotation) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.rotation = rotation;
        }
    }
}