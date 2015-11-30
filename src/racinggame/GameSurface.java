package racinggame;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameSurface extends JPanel implements ActionListener {
    
    private Timer timer;
    private Car car;
    private BufferedImage track;
    private final int DELAY = 10;
    
    GameSurface() {
        initWindow();
    }
    
    private void initWindow() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        
        try {
            track = ImageIO.read(getClass().getResourceAsStream("/resources/track2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        car = new Car(this, track);
        
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);
    }
    
    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(track, 0, 0, null);
        int x = car.getX();
        int y = car.getY();
        int w = car.getImage().getWidth();
        int h = car.getImage().getHeight();
        double a = car.getAngle() + Math.PI/2;
        g2d.rotate(a, x+w/2, y+h/2);
        g2d.drawImage(car.getImage(), x, y, null);       
        Toolkit.getDefaultToolkit().sync();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        car.move();
        repaint();
    }
    
    private class TAdapter extends KeyAdapter {
        
        @Override
        public void keyReleased(KeyEvent e) {
            car.keyReleased(e);
        }
        
        @Override
        public void keyPressed(KeyEvent e) {
            car.keyPressed(e);
        }
    }
    
}
