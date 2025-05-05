package f2;

import javax.swing.*;
import java.io.File;
import java.awt.Image;

public class ProductImageSetter {
    public void setProductImage(String style, JLabel imageLabel) {
        String imageName = style.toLowerCase().replace(" ", "_");
        String[] extensions = {".png", ".jpg", ".jpeg"};
        boolean found = false;

        for (String ext : extensions) {
            File file = new File("images/" + imageName + ext);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                found = true;
                break;
            }
        }

        if (!found) {
            ImageIcon fallback = new ImageIcon("images/noimage.png");
            Image fallbackImg = fallback.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(fallbackImg));
        }

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
}