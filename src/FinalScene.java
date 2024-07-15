import javax.swing.*;
import java.awt.*;

public class FinalScene extends JPanel implements Runnable {

    private boolean showSecondMessage = false;

    public FinalScene() {
        new Thread(this).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showSecondMessage) {
            drawSecondMessage(g);
        } else {
            drawFirstMessage(g);
        }
    }

    private void drawFirstMessage(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Próximamente en el tercer parcial", 100, 300);
        g.drawString("solo aquí con Miss Rosa", 100, 400);
    }

    private void drawSecondMessage(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Páseme por favor,", 100, 300);
        g.drawString("No he dormido nada :,v", 100, 400);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000); // Espera 5 segundos antes de mostrar el segundo mensaje
            showSecondMessage = true;
            repaint();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
