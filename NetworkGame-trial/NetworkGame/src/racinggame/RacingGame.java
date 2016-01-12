package racinggame;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class RacingGame extends JFrame {
    private Car car;
    
    RacingGame(Car car1) {
        car = car1;
        initUI();
    }
    
    public void initUI() {
        add(new GameSurface(car));
        setSize(800, 635);
        setResizable(false);
        setTitle("2D Racing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    public static void start(final Car car1) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                RacingGame rg = new RacingGame(car1);
                rg.setVisible(true);
            }
        });
    }
}
