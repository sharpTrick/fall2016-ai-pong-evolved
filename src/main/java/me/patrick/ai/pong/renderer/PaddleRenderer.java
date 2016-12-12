package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.util.HasIndex;
import me.patrick.ai.pong.model.GameModel;

import java.awt.*;

/**
 * Created by patrick on 12/1/16.
 */
public class PaddleRenderer extends RendererTemplate implements HasIndex {
    private int paddle=0;

    public int getIndex() {
        return paddle;
    }

    public void setIndex(int paddle) {
        this.paddle = paddle;
    }

    @Override
    public double getX(GameModel model) {
        return model.getPaddleValue(paddle, GameModel.PADDLE_X);
    }

    @Override
    public double getY(GameModel model) {
        return model.getPaddleValue(paddle, GameModel.PADDLE_Y);
    }

    @Override
    public double getWidth(GameModel model) {
        return model.getPaddleValue(paddle, GameModel.PADDLE_WIDTH);
    }

    @Override
    public double getHeight(GameModel model) {
        return model.getPaddleValue(paddle, GameModel.PADDLE_HEIGHT);
    }

    @Override
    public double getRotate(GameModel model) {
        return 0;//model.getPaddleValue(paddle, GameModel.PADDLE_ROT);
    }

    private static final Stroke OUTLINE = new BasicStroke(3);
    @Override
    public void renderObject(Graphics2D graphics, int width, int height) {
        graphics.setPaint(Color.red);
        graphics.fillRect(0,0,width,height);
        graphics.setStroke(OUTLINE);
        graphics.setPaint(Color.black);
        graphics.drawRect(0,0,width,height);
    }

}
