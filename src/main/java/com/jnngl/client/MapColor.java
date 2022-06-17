package com.jnngl.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

/**
 * Convert {@link Color} to map color
 */
public class MapColor {

    /**
     * Colors used in map
     */
    public static Color[] colors;

    /**
     * Cached values
     */
    private static byte[] cache;

    public static void setPalette(Color[] palette) {
        colors = palette;
    }

    public static void cachePalette() {
        cache = new byte[0x1000];
        for(int i = 0x000; i <= 0xFFF; i++)
            cache[i] = matchColor(new Color((i & 0xF00) >> 4, i & 0xF0, (i & 0xF) << 4));
    }

    public static byte matchColorFast(Color color) {
        if(cache == null) return matchColor(color);
        int rgb = color.getRGB();
        return cache[(rgb & 0xF00000) >> 12 | (rgb & 0xF000) >> 8 | (rgb & 0xF0) >> 4];
    }

    private static double getDistance(Color c1, Color c2) {
        double rMean = (double)(c1.getRed() + c2.getRed()) / 2.0D;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2.0D + rMean / 256.0D;
        double weightG = 4.0D;
        double weightB = 2.0D + (255.0D - rMean) / 256.0D;
        return weightR * r * r + weightG * g * g + weightB * (double)b * (double)b;
    }

    /**
     * Deprecated function from bukkit
     * @param color {@link Color}
     * @return Nearest color that can be used when drawing map
     */
    public static byte matchColor(Color color) {
        if (color.getAlpha() < 128) {
            return 0;
        } else {
            int index = 0;
            double best = -1.0D;

            for(int i = 4; i < colors.length; ++i) {
                double distance = getDistance(color, colors[i]);
                if (distance < best || best == -1.0D) {
                    best = distance;
                    index = i;
                }
            }

            return (byte)(index < 128 ? index : -129 + (index - 127));
        }
    }

    /**
     * Converts image to byte color index array
     */
    public static byte[] toByteArray(BufferedImage data) {
        byte[] bytes = new byte[data.getWidth()*data.getHeight()];
        int[] pixels = data.getRGB(0, 0, data.getWidth(), data.getHeight(), null, 0, data.getWidth());
        for(int i = 0; i < pixels.length; i++)
            bytes[i] = matchColorFast(new Color(pixels[i]));
        return bytes;

    }

}
