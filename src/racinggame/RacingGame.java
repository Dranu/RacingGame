package racinggame;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class RacingGame extends JFrame {
    
    RacingGame() {
        initUI();
    }
    
    public void initUI() {
        add(new GameSurface());
        setSize(800, 600);
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
