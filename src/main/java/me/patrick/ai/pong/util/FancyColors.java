package me.patrick.ai.pong.util;

import java.awt.*;
import java.time.Instant;

/**
 * Created by patrick on 12/2/16.
 */
public class FancyColors {

    private static FancyColors ourInstance = new FancyColors();

    public static FancyColors getInstance() {
        return ourInstance;
    }

    private FancyColors() {}

    private static final double PI23 = Math.PI*2/3d;
    private static final double PI43 = PI23*2;
    public Color calculateFancyColor(double ratio){
        long colorChanger = Instant.now().toEpochMilli();

        double radians = ratio*2*Math.PI+(colorChanger/1000d);
        int r = (int) (Math.sin(radians) * 127 + 128);
        int g = (int) (Math.sin(radians+PI23) * 127 + 128);
        int b = (int) (Math.sin(radians+PI43) * 127 + 128);

        return new Color(r,g,b);
    }
}
