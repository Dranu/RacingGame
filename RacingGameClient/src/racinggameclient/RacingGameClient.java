package racinggameclient;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import javax.swing.JFrame;

public class RacingGameClient extends JFrame {
    
    private GameSurface gs;
    
    RacingGameClient(String host) {
        initUI(host);
    }
    
    public void initUI(String host) {
        gs = new GameSurface(host);
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
        
    //    if (args.length == 0 ){
    //        System.out.println("Give a proper host name");
            //System.out.println(args.length);
    //    }
    //    else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    RacingGameClient rg = new RacingGameClient(args[0]);
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
      //  }
    }
}
