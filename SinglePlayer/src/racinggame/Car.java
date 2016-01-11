package racinggame;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Car {
    
    private double x, y; // position of the car
    private double vx, vy; // velocity on each axis
    private double drag; // how fast car slows down
    private double angle; // rotation of the car, in radians
    private double angVel; // speed the car is spinning, in radians
    private double angDrag; // how fast the car stops spinning
    private double power; // how fast car can accelerate
    private double turnSpeed; // how fast to turn
    
    private int throttle; // 1 = forward , 0 = off, -1 = reverse
    private int steering; // 1 = right, 0 = center, -1 = left
    
    private BufferedImage img;
    private BufferedImage track;
    private GameSurface gamesurface;
    
    Car(GameSurface gamesurface, BufferedImage track) {
        this.gamesurface = gamesurface;
        this.track = track;
        initCar();
    }
    
    public void initCar() {
        try {
            img = ImageIO.read(getClass().getResourceAsStream("/resources/car1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Set parameters
        this.x = 41;
        this.y = 300;
        this.vx = 0;
        this.vy = 0;
        this.drag = 0.9;
        this.angle = -Math.PI/2;
        this.angVel = 0;
        this.angDrag = 0.9;
        this.power = 0.2;
        this.turnSpeed = 0.0087;
        
        this.throttle = 0;
        this.steering = 0;
    }
    
    public void move() {
        
        if (throttle != 0) {
            if (throttle == 1) { // forward
                vx += Math.cos(angle)*power;
                vy += Math.sin(angle)*power;
            }
            if (throttle == -1) { // reverse
                vx -= Math.cos(angle)*power*0.5;
                vy -= Math.sin(angle)*power*0.5;
            }
            if (steering == -1) { // left
                angVel -= turnSpeed;
            }
            if (steering == 1) { // right
                angVel += turnSpeed;
            }
        }
        
        // Update position
        x += vx;
        y += vy;
        angle += angVel;
        
        if (collisiondetect(getX(), getY()) == false) {
            vx *= drag;
            vy *= drag;
            angVel *= angDrag;
        }
        else {
            vx *= drag*0.5;
            vy *= drag*0.5;
            angVel *= angDrag*0.5;
        }
        
        // reset the angle if needed
        // if (angle > Math.PI*2) angle -= Math.PI*2;
        // else if (angle < -Math.PI*2) angle += Math.PI*2;
        
    }
    
    public boolean collisiondetect(int X, int Y) {
        
        // The angle the image is drawn 
        double imageAngle = angle + Math.PI/2;
        // Calculate center point of the car
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int cx = X + w/2;
        int cy = Y + h/2;
        
        // Calculate corner coordinates of the car
        int[] lu_coords = getCorner(X,Y,cx,cy,imageAngle);
        int[] ru_coords = getCorner(X+w,Y,cx,cy,imageAngle);
        int[] ll_coords = getCorner(X,Y+h,cx,cy,imageAngle);
        int[] rl_coords = getCorner(X+w,Y+h,cx,cy,imageAngle);
        
        // Get colors of the game surface for each corner coordinate
        int luc = track.getRGB(lu_coords[0], lu_coords[1]);
        int ruc = track.getRGB(ru_coords[0], ru_coords[1]);
        int llc = track.getRGB(ll_coords[0], ll_coords[1]);
        int rlc = track.getRGB(rl_coords[0], rl_coords[1]);
        
        int tc = new Color(127,127,127).getRGB(); // track color
        //field color: 0,229, 7
        //redline color: 229, 11, 0 - need to slow the speed a little bit, maybe to 0.8 times
        
        if (luc!=tc || ruc!=tc || llc!=tc || rlc!=tc) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public int[] getCorner(int X, int Y, int CX, int CY, double Angle) {
        
        int[] coords = new int[2];
        
        double RX = (X-CX)*Math.cos(Angle)-(Y-CY)*Math.sin(Angle);
        double RY = (X-CX)*Math.sin(Angle)+(Y-CY)*Math.cos(Angle);
        
        coords[0] = (int)Math.floor(RX + CX);
        coords[1] = (int)Math.floor(RY + CY);
        
        return coords;
    }
    
    public int getX() {
        return (int)Math.floor(x);
    }
    
    public int getY() {
        return (int)Math.floor(y);
    }
    
    public double getAngle() {
        return angle;
    }
    
    public BufferedImage getImage() {
        return img;
    }
    
    public void keyPressed(KeyEvent e) {
        
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            steering = -1;
        }
        if (key == KeyEvent.VK_RIGHT) {
            steering = 1;
        }
        if (key == KeyEvent.VK_UP) {
            throttle = 1;
        }
        if (key == KeyEvent.VK_DOWN) {
            throttle = -1;
        }
    }
    
    public void keyReleased(KeyEvent e) {
        
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            steering = 0;
        }
        if (key == KeyEvent.VK_RIGHT) {
            steering = 0;
        }
        if (key == KeyEvent.VK_UP) {
            throttle = 0;
        }
        if (key == KeyEvent.VK_DOWN) {
            throttle = 0;
        }
    }
}
