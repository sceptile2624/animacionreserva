import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.Random;

public class HojaCanada extends JPanel implements Runnable {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color LINE_COLOR = Color.RED;
    private static final Color FILL_COLOR = Color.RED;
    private static final int DRAWING_DURATION = 10000; // 10 segundos
    private static final String SOUND_FILE = "sonido.wav"; // Ruta al archivo de sonido

    private BufferedImage canvas;
    private JLabel label;
    private boolean isDrawingComplete = false;

    public HojaCanada() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        label = new JLabel(new ImageIcon(canvas));
        add(label);
        playSound(SOUND_FILE); // Reproduce el sonido al inicio
        new Thread(this::drawAndFill).start();
    }

    private void playSound(String filename) {
        try {
            File soundFile = new File(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawAndFill() {
        int[] xPoints = {1, 5, 4, 9, 7, 8, 5, 5, 3, 0, -2, -4, -3, -4, -3, -5, -5, -8, -7, -9, -4, -5, 0, 2, 2, 1};
        int[] yPoints = {-3, -4, -3, 1, 2, 5, 4, 5, 4, 10, 7, 8, 3, 8, 3, 6, 4, 5, 2, 1, -3, -4, -3, -7, -6, -3};

        for (int i = 0; i < xPoints.length; i++) {
            xPoints[i] = (xPoints[i] + 10) * WIDTH / 40;
            yPoints[i] = HEIGHT - ((yPoints[i] + 15) * HEIGHT / 40);
        }

        // Draw outline gradually
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < xPoints.length - 1; i++) {
            drawLineDDA(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], LINE_COLOR);
            label.repaint();
            try {
                Thread.sleep(DRAWING_DURATION / xPoints.length);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        drawLineDDA(xPoints[xPoints.length - 1], yPoints[yPoints.length - 1], xPoints[0], yPoints[0], LINE_COLOR);

        startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < DRAWING_DURATION) {
            fillScanline(xPoints, yPoints, FILL_COLOR);
            label.repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isDrawingComplete = true;

        // Draw text
        drawText();
        label.repaint();

        // Switch to AnimatedScene after a delay
        try {
            Thread.sleep(2000); // Wait for 2 seconds before switching
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switchToAnimatedScene();
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

    private void fillScanline(int[] xPoints, int[] yPoints, Color fillColor) {
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int y : yPoints) {
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        for (int y = minY; y <= maxY; y++) {
            java.util.List<Integer> nodeX = new java.util.ArrayList<>();
            int nodes = 0, j = xPoints.length - 1;
            for (int i = 0; i < xPoints.length; i++) {
                if (yPoints[i] < y && yPoints[j] >= y || yPoints[j] < y && yPoints[i] >= y) {
                    nodeX.add(nodes++, (int) (xPoints[i] + (y - yPoints[i]) / (double) (yPoints[j] - yPoints[i]) * (xPoints[j] - xPoints[i])));
                }
                j = i;
            }
            nodeX.sort(Integer::compareTo);
            for (int i = 0; i < nodes - 1; i += 2) {
                for (int x = nodeX.get(i); x <= nodeX.get(i + 1); x++) {
                    putPixel(x, y, fillColor);
                }
            }
        }
    }

    private void drawText() {
        Graphics2D g2d = (Graphics2D) canvas.getGraphics();
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.drawString("Un trailer de Nando Ramirez Producciones", WIDTH - 650, 300);
        g2d.drawString("21110137", WIDTH - 650, 350);
        g2d.drawString("Esperalo pronto", WIDTH - 650, 400);
        g2d.dispose();
    }

    private void switchToAnimatedScene() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new AnimatedScene());
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void run() {
        drawAndFill();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Creditos: el camino");
        HojaCanada hojaCanada = new HojaCanada();
        frame.add(hojaCanada);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        new Thread(hojaCanada).start();
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