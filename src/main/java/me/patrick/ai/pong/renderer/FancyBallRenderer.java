package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.model.GameModel;
import me.patrick.ai.pong.util.FancyColors;

import java.awt.*;

/**
 * Created by patrick on 12/2/16.
 */
public class FancyBallRenderer extends BallRenderer {
    private static final int BALL_SLICES = 6;
    private static final int SLICE_ARC_ANGEL = 360/BALL_SLICES;
    private int ballCount=1;

    @Override
    public void render(GameModel model, Graphics2D graphics, int canvasWidth, int canvasHeight) {
        ballCount = model.getBallCount();
        super.render(model, graphics, canvasWidth, canvasHeight);
    }

    @Override
    public void renderObject(Graphics2D graphics, int width, int height) {
        double ratio = getIndex()/(double)ballCount;
        Color fancyColor = FancyColors.getInstance().calculateFancyColor(ratio);
        for(int i=0; i<BALL_SLICES; i++){
            graphics.setPaint(i%2==0?Color.black:fancyColor);
            graphics.fillArc(0,0,width,height,(i*360)/BALL_SLICES,SLICE_ARC_ANGEL);
        }
        super.renderObject(graphics, width, height);
    }


}
