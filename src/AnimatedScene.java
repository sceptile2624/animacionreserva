import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimatedScene extends JPanel implements Runnable {
    private BufferedImage canvas;
    private int cloudX1, cloudX2;
    private List<Leaf> leaves;
    private int[] xPoints = {1, 5, 4, 9, 7, 8, 5, 5, 3, 0, -2, -4, -3, -4, -3, -5, -5, -8, -7, -9, -4, -5, 0, 2, 2, 1};
    private int[] yPoints = {-3, -4, -3, 1, 2, 5, 4, 5, 4, 10, 7, 8, 3, 8, 3, 6, 4, 5, 2, 1, -3, -4, -3, -7, -6, -3};

    public AnimatedScene() {
        canvas = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        cloudX1 = 0;
        cloudX2 = 640;
        leaves = new ArrayList<>();
        initializeLeaves();
        new Thread(this).start();
    }

    private void initializeLeaves() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int leafX = random.nextInt(1280);
            int leafY = random.nextInt(720);
            int leafSize = 20;
            int leafRotation = random.nextInt(360); // Rotación aleatoria de la hoja
            leaves.add(new Leaf(leafX, leafY, leafSize, leafRotation));
        }
    }

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

    private void updateClouds() {
        cloudX1 += 2;
        cloudX2 += 2;
        if (cloudX1 > 1280) {
            cloudX1 = -200;
        }
        if (cloudX2 > 1280) {
            cloudX2 = -200;
        }
    }

    private void putPixel(int x, int y, Color color) {
        if (x >= 0 && x < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
            canvas.setRGB(x, y, color.getRGB());
        }
    }



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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawScene();
        g.drawImage(canvas, 0, 0, null);
    }

    private void drawScene() {
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                putPixel(x, y, Color.BLUE);
            }
        }

        // dibujar el circulo con iteraciones
        drawCloud(cloudX1, 50);
        drawCloud(cloudX2, 100);

        // Draw the road
        for (int x = 0; x < 1280; x++) {
            for (int y = 500; y < 720; y++) {
                putPixel(x, y, Color.GRAY);
            }
        }
        for (int i = 0; i < 1280; i += 40) {
            for (int y = 600; y < 610; y++) {
                for (int x = i; x < i + 20; x++) {
                    putPixel(x, y, Color.YELLOW);
                }
            }
        }

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
            drawLineBresenham(rotatedXPoints[i], rotatedYPoints[i], rotatedXPoints[i + 1], rotatedYPoints[i + 1], new Color(139, 69, 19));
        }
        drawLineBresenham(rotatedXPoints[rotatedXPoints.length - 1], rotatedYPoints[rotatedXPoints.length - 1],
                rotatedXPoints[0], rotatedYPoints[0], new Color(139, 69, 19));
        fillLeaf(rotatedXPoints, rotatedYPoints, new Color(139, 69, 19));
    }

    private void fillLeaf(int[] xPoints, int[] yPoints, Color color) {
        // Get the bounding box of the leaf
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

        // Use the even-odd rule to determine if a point is inside the leaf
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

    private void switchToCamion() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new Camion());
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime >= 10000) {
                switchToCamion();
                break; // Salir del bucle después de cambiar a HojaCanada
            }
            updateLeaves();
            updateClouds();
            repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("El camino y la vida");
        AnimatedScene scene = new AnimatedScene();
        frame.add(scene);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
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
