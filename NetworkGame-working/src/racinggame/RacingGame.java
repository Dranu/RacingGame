package racinggame;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class RacingGame extends JFrame {
    
    RacingGame() {
        initUI();
    }
    
    public void initUI() {
        GameSurface gs = new GameSurface();
        add(gs);
        new Thread(gs).start();
        setSize(806, 629);
        setResizable(false);
        setTitle("2D Racing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                RacingGame rg = new RacingGame();
                rg.setVisible(true);
            }
        });
    }
}
