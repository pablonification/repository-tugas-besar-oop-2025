package com.spakborhills.model.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Utility {
    public static BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);

        g2.dispose();
        return scaled;
    }
}
