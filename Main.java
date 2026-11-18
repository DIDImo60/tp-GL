import javax.swing.SwingUtilities;
import GUI.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            mainFrame frame = new mainFrame();
            frame.setVisible(true);
        });
        
    }
    
}
