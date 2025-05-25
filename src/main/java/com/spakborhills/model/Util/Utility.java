package com.spakborhills.model.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SuppressWarnings("DataFlowIssue")
public class Utility {

    private Utility() {}
    private static Utility INSTANCE = null;

    public static Utility getInstance() {
        if(INSTANCE == null) INSTANCE = new Utility();
        return INSTANCE;
    }
    public static BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);

        g2.dispose();
        return scaled;
    }

    public BufferedImage setup(String imagePath, int width, int height) {
        BufferedImage image = null;
        try{
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
            image = Utility.scaleImage(image, width, height);

        }catch (IOException e) {
            throw new RuntimeException("Failed to load image. " + e);
        }

        return image;
    }
}
