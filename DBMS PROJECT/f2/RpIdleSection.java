package f2;

import javax.swing.*;
import java.awt.*;

public class RpIdleSection extends JFrame {

    public RpIdleSection() {
        setTitle("RpIdleSection");
        setSize(400, 300);
        setLocationRelativeTo(null); // center on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Simple label or panel to show this frame loaded
        JLabel label = new JLabel("Welcome to RpIdleSection", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        add(label);
    }
}
