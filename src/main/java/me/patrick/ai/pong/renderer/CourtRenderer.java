package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.model.GameModel;

import java.awt.*;

/**
 * Created by patrick on 12/1/16.
 */
public class CourtRenderer implements RenderStrategy {
    @Override
    public void render(GameModel model, Graphics2D graphics, int width, int height){
        graphics.setPaint(Color.black);
        graphics.fillRect(0,0,width,height);

        graphics.setPaint(Color.darkGray);
        graphics.drawRect(0, 0, width-1, height - 1);
        graphics.drawLine(width / 2, 0, width / 2, height-1);
        if(2<model.getPaddleCount()) graphics.drawLine(0, height/2, width-1, height/2);
    }
}
