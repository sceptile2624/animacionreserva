import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Gato extends JPanel implements Runnable {

    private BufferedImage canvas;
    private List<Leaf> leaves;
    private int catX;
    private int catY;
    private boolean showMessage = false;
    private long stopTime;
    private int[] xPoints = {1, 5, 4, 9, 7, 8, 5, 5, 3, 0, -2, -4, -3, -4, -3, -5, -5, -8, -7, -9, -4, -5, 0, 2, 2, 1};
    private int[] yPoints = {-3, -4, -3, 1, 2, 5, 4, 5, 4, 10, 7, 8, 3, 8, 3, 6, 4, 5, 2, 1, -3, -4, -3, -7, -6, -3};

    public Gato() {
        canvas = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        leaves = new ArrayList<>();
        initializeLeaves();
        catX = 0; // Posición inicial del gato
        catY = 360;
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
            leaf.y += 4; // Velocidad de caída más lenta
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

    private void updateCat() {
        if (catX < 540) { // Detener al gato a mitad de la pantalla
            catX += 4; // Velocidad de movimiento del gato más lenta
        } else if (!showMessage) {
            showMessage = true; // Mostrar el mensaje cuando el gato se detiene
            stopTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - stopTime > 3000) { // Espera 3 segundos antes de cambiar de escena
            switchToAguila();
        }
    }

    private void switchToAguila() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new Aguila());
        frame.revalidate();
        frame.repaint();
    }

    private void putPixel(int x, int y, Color color) {
        if (x >= 0 && x < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
            canvas.setRGB(x, y, color.getRGB());
        }
    }

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

    private void drawScene() {
        // Clear the canvas
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                putPixel(x, y, Color.CYAN);
            }
        }

        // Draw the grass
        for (int x = 0; x < 1280; x++) {
            for (int y = 500; y < 720; y++) {
                putPixel(x, y, Color.GREEN);
            }
        }

        // Draw the flowers
        drawFlowers();

        // Draw the birds
        drawBirds();

        // Draw the falling leaves
        for (Leaf leaf : leaves) {
            drawRotatedLeaf(leaf);
        }

        // Draw the cat
        drawCat();

        // Draw the "miau" message if needed
        if (showMessage) {
            drawString("Miau", 600, 300, Color.BLACK, new Font("Arial", Font.BOLD, 40));
        }
    }

    private void drawFlowers() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(1280);
            int y = 500 + random.nextInt(220);
            drawCircleBresenham(x, y, 5, Color.RED);
        }
    }

    private void drawBirds() {
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(1280);
            int y = random.nextInt(200);
            drawArc(x, y, 20, 10, 0, 180, Color.BLACK);
            drawArc(x + 20, y, 20, 10, 0, 180, Color.BLACK);
        }
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

    private void drawCat() {
        int scale = 20;
        int offsetX = catX;
        int offsetY = catY;

        int[][] catBody = {
                {1, 14}, {0, 8}, {0, 4}, {3, 0}, {5, 0}, {5, 2}, {3, 4}, {2, 4}, {2, 7}, {4, 5},
                {6, 5}, {8, 7}, {8, 3}, {10, 5}, {14, 5}, {16, 3}, {16, 9}, {15, 10}, {13, 11}, {11, 11},
                {9, 10}, {8, 9}, {8, 12}, {9, 12}, {9, 14}, {6, 14}, {6, 10}, {4, 10}, {3, 11}, {3, 12},
                {5, 12}, {5, 14}
        };

        int[][] leftEye = {{9, 6}, {11, 6}, {11, 8}, {9, 8}};
        int[][] rightEye = {{13, 6}, {15, 6}, {15, 8}, {13, 8}};
        int[][] nose = {{11, 9}, {12, 8}, {13, 9}};
        int[][] mouth = {{11, 10}, {12, 10}, {13, 10}};

        // Dibujar cuerpo del gato
        for (int i = 0; i < catBody.length - 1; i++) {
            drawLineBresenham(catBody[i][0] * scale + offsetX, catBody[i][1] * scale + offsetY,
                    catBody[i + 1][0] * scale + offsetX, catBody[i + 1][1] * scale + offsetY, Color.ORANGE);
        }
        drawLineBresenham(catBody[catBody.length - 1][0] * scale + offsetX, catBody[catBody.length - 1][1] * scale + offsetY,
                catBody[0][0] * scale + offsetX, catBody[0][1] * scale + offsetY, Color.ORANGE);
        fillPolygon(catBody, offsetX, offsetY, scale, Color.ORANGE);

        // Dibujar los ojos del gato
        fillPolygon(leftEye, offsetX, offsetY, scale, Color.WHITE);
        fillPolygon(rightEye, offsetX, offsetY, scale, Color.WHITE);

        // Dibujar la nariz del gato
        fillPolygon(nose, offsetX, offsetY, scale, Color.PINK);

        // Dibujar la boca del gato
        fillPolygon(mouth, offsetX, offsetY, scale, Color.BLACK);
    }

    private void fillPolygon(int[][] points, int offsetX, int offsetY, int scale, Color color) {
        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];

        for (int i = 0; i < points.length; i++) {
            xPoints[i] = points[i][0] * scale + offsetX;
            yPoints[i] = points[i][1] * scale + offsetY;
        }

        fillLeaf(xPoints, yPoints, color);
    }

    private void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, Color color) {
        double startRad = Math.toRadians(startAngle);
        double arcRad = Math.toRadians(arcAngle);
        int steps = 100;
        double stepSize = arcRad / steps;
        for (int i = 0; i < steps; i++) {
            int x1 = (int) (x + width / 2 * Math.cos(startRad + i * stepSize));
            int y1 = (int) (y + height / 2 * Math.sin(startRad + i * stepSize));
            int x2 = (int) (x + width / 2 * Math.cos(startRad + (i + 1) * stepSize));
            int y2 = (int) (y + height / 2 * Math.sin(startRad + (i + 1) * stepSize));
            drawLineBresenham(x1, y1, x2, y2, color);
        }
    }

    private void drawString(String text, int x, int y, Color color, Font font) {
        Graphics2D g2dCanvas = canvas.createGraphics();
        g2dCanvas.setColor(color);
        g2dCanvas.setFont(font);
        g2dCanvas.drawString(text, x, y);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawScene();
        g.drawImage(canvas, 0, 0, null);
    }

    @Override
    public void run() {
        while (true) {
            updateLeaves();
            updateCat();
            repaint();
            try {
                Thread.sleep(100); // Velocidad de actualización más lenta
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fin del recorrido: camino");
        Gato gato = new Gato();
        frame.add(gato);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

