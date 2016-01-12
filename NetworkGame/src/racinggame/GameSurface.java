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
import java.io.*;
import java.net.*;
import java.util.*;

public class GameSurface extends JPanel implements ActionListener {
    
    private Timer timer;
    public Car car;
    private BufferedImage track;
    private BufferedImage carImage;
    private final int DELAY = 10;
    private int x = -1;
    private int y = -1;
    private int id = -1;
    //public List<String[]> Carlist = new ArrayList();
    public static ArrayList<String[]> Carlist = new ArrayList<String[]>();

    
    GameSurface(Car car1) {
        car = car1;
        initWindow();
    }
    
    private void initWindow() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        
        try {
            //removed AsStream because it produced an error - Jiri
            track = ImageIO.read(getClass().getResource("/resources/track3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Kekkura");
        //car = new Car(this, track, ID);
        
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);
    }
    
    private void render(Graphics g) {
        id = car.getID();
        int i = 0;
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(track, 0, 0, null);
        //carlist = ServerThread.getList();
        //x = car.getX();
        //y = car.getY();
        //System.out.println("Render ID: " + id);
        //System.out.println("Game Surface Size: "+ GameSurface.Carlist.size());
        //if (id == 0){
            for(i=0;i<Carlist.size();i++){
                AffineTransform old = g2d.getTransform();
                String[] CarlistArray = Carlist.get(i);
                x =Integer.parseInt(CarlistArray[2]);
                y =Integer.parseInt(CarlistArray[3]);
                int w = 20;//car.getImage().getWidth();
                int h = 14;//car.getImage().getHeight();
                double a = Double.parseDouble(CarlistArray[4]) + Math.PI/2;
                g2d.rotate(a, x+w/2, y+h/2);
                try {
            //removed AsStream because it produced an error - Jiri
                    carImage = ImageIO.read(getClass().getResource("/resources/car"+(i+1)+".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                g2d.drawImage(carImage, x, y, null);
                g2d.setTransform(old);  //fixes the rotation issue     
                Toolkit.getDefaultToolkit().sync();        
            }
       /* }
        else{
            x = car.getX();
            y = car.getY();
            int w = car.getImage().getWidth();
            int h = car.getImage().getHeight();
            double a = car.getAngle() + Math.PI/2;
            g2d.rotate(a, x+w/2, y+h/2);
            g2d.drawImage(car.getImage(), x, y, null);       
            Toolkit.getDefaultToolkit().sync();
        }*/
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
