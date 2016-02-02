package racinggame;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

public class Car {
    
    private int id;
    
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
    
    ArrayList<Color> checkpointList = new ArrayList<Color>(); //Make an arraylist for the checkpoints
    
    
    private int prevCheck;
    private int curCheck;
    private int nextCheck;
    private int checkpoint;
    private int lap;
    
    private BufferedImage img;
    private BufferedImage track;
    
    
    
    
    Car(BufferedImage track, int id) {
        this.track = track;
        this.id = id;
        initCar();
    }
    
    public void initCar() {
        try {
            // select car image based on id!
            img = ImageIO.read(getClass().getResourceAsStream("/resources/car"+(this.id+1)+".png"));
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
        
        this.prevCheck = 0;
        this.curCheck = 0;
        this.nextCheck = 0;
        this.lap = 1;
        //Add the checkpoints to the arraylist
        this.checkpointList.add(new Color(255,255,255));//finishline
        this.checkpointList.add(new Color(126,126,126));// *1) 126,126,126 -
        this.checkpointList.add(new Color(127,126,126));// *2) 127,126,126 -
        this.checkpointList.add(new Color(128,126,126));// *3) 128,126,126 -
        this.checkpointList.add(new Color(126,127,126));// *4) 126,127,126 
        this.checkpointList.add(new Color(126,128,126));// *5) 126,128,126
        this.checkpointList.add(new Color(126,126,127));// *6) 126,126,127
        this.checkpointList.add(new Color(126,126,128));// *7) 126,126,128
        this.checkpointList.add(new Color(128,128,128));// *8) 128,128,128
        this.checkpointList.add(new Color(126,127,128));// *9) 126,127,128
        this.checkpoint = 0;
    
        
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
        
        switch (terraindetect(getX(), getY())) {
            case -1:
                // on track image border
                x -= vx;
                y -= vy;
                angle -= angVel;
                vx = 0;
                vy = 0;
                angVel = 0;
                break;
            case 0:
                // on track
                vx *= drag;
                vy *= drag;
                angVel *= angDrag;
                break;
            case 1:
                // on redline
                vx *= drag*0.9;
                vy *= drag*0.9;
                angVel *= angDrag*0.9;
                break;
            case 2:
                // on grass
                vx *= drag*0.6;
                vy *= drag*0.6;
                angVel *= angDrag*0.6;
                break;
        }
        
        // reset the angle if needed
        // if (angle > Math.PI*2) angle -= Math.PI*2;
        // else if (angle < -Math.PI*2) angle += Math.PI*2;
        
    }
    
    public int terraindetect(int X, int Y) {
        
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
        
        
        // Check for track image borders
        int [] xcoords = {lu_coords[0], ru_coords[0], ll_coords[0], rl_coords[0]};
        int [] ycoords = {lu_coords[1], ru_coords[1], ll_coords[1], rl_coords[1]};
        for (int i=0; i<4; i++) {
            if (xcoords[i] < 0 || xcoords[i] >= 800 ||
                ycoords[i] < 0 || ycoords[i] >= 600) {
                // coordinates out of bounds
                return -1;
            }
        }
        
        // Get colors of the game surface for each corner coordinate
        int luc = track.getRGB(lu_coords[0], lu_coords[1]);
        int ruc = track.getRGB(ru_coords[0], ru_coords[1]);
        int llc = track.getRGB(ll_coords[0], ll_coords[1]);
        int rlc = track.getRGB(rl_coords[0], rl_coords[1]);
        
        //int tc = new Color(127,127,127).getRGB(); // track color
        //http://stackoverflow.com/questions/7604814/best-way-to-format-multiple-or-conditions-in-an-if-statement-java
        Set<Integer> corners = new HashSet<Integer>(Arrays.asList(luc,ruc,llc,rlc));
        int field = new Color(0,229,7).getRGB();      //field color: 0,229, 7
        int redline = new Color(229,11,0).getRGB(); //redline color: 229, 11, 0 
     
        if(checkpoint == 9){ //Last checkpoint cleared, next should be finishline
            if ( corners.contains(checkpointList.get(0).getRGB() )){
                checkpoint = 0;
                System.out.println(checkpoint + ". Checkpoint: " + checkpointList.get(checkpoint).getRGB());
                lap++;
            }
        }
        else if ( corners.contains(checkpointList.get(checkpoint + 1).getRGB()) ) {
            checkpoint++;
            System.out.println(checkpoint + ". Checkpoint: " + checkpointList.get(checkpoint).getRGB());
        }

        
        
        if (corners.contains(field)) {//(luc==field || ruc==field || llc==field || rlc==field ){
            // on grass (may want to change if-clause to check for grass color)
            return 2;
        }
        else if(corners.contains(redline)){//(luc==redline || ruc==redline || llc==redline || rlc==redline){
            return 1;
        }
        else {
            // on track
            return 0;
        }
    }
    
    

    
    
    public int getLap(){
        return lap;
    }
    
    public int[] getCorner(int X, int Y, int CX, int CY, double Angle) {
        
        int[] coords = new int[2];
        
        double RX = (X-CX)*Math.cos(Angle)-(Y-CY)*Math.sin(Angle);
        double RY = (X-CX)*Math.sin(Angle)+(Y-CY)*Math.cos(Angle);
        
        coords[0] = (int)Math.floor(RX + CX);
        coords[1] = (int)Math.floor(RY + CY);
        
        return coords;
    }
    
    public int getID() {
        return id;
    }
    
    public int getX() {
        return (int)Math.floor(x);
    }
    
    public int getY() {
        return (int)Math.floor(y);
    }
    
    public void setID(int id) {
        this.id = id;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public void setAngle(double a) {
        this.angle = a;
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
