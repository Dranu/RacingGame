package racinggame;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class RacingGame extends JFrame {
    
    private GameSurface gs;
    
    RacingGame() {
        initUI();
    }
    
    public void initUI() {
        gs = new GameSurface();
        add(gs);
        new Thread(gs).start();
        setSize(806, 629);
        setResizable(false);
        setTitle("2D Racing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    public int getPlayerID() {
        return gs.getCarID();
    }
    
    public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                RacingGame rg = new RacingGame();
                // wait if we are not the last person joined i.e. id = 3
                if (rg.getPlayerID() < 3) {
                    synchronized (GameSurface.ready) {
                        try {
                            GameSurface.ready.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                rg.setVisible(true);
            }
        });
    }
}
