package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.util.HasIndex;
import me.patrick.ai.pong.model.GameModel;

import java.awt.*;

/**
 * Created by patrick on 12/1/16.
 */
public class BallRenderer extends RendererTemplate implements HasIndex {
    private int ball=0;

    public int getIndex() {
        return ball;
    }

    public void setIndex(int ball) {
        this.ball = ball;
    }

    @Override
    public double getX(GameModel model) {
        return model.getBallValue(ball,GameModel.BALL_X);
    }

    @Override
    public double getY(GameModel model) {
        return model.getBallValue(ball,GameModel.BALL_Y);
    }

    @Override
    public double getWidth(GameModel model) {
        return GameModel.BALL_RADIUS*2;
    }

    @Override
    public double getHeight(GameModel model) {
        return GameModel.BALL_RADIUS*2;
    }

    @Override
    public double getRotate(GameModel model) {
        return 0;
//        return model.getBallValue(ball,GameModel.BALL_ROT);
    }

    @Override
    public void renderObject(Graphics2D graphics, int width, int height) {
        graphics.setPaint(Color.DARK_GRAY);
        graphics.drawArc(0,0, width, height, 0, 360);
    }

}
