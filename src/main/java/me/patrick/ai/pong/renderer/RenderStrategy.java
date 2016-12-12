package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.model.GameModel;

import java.awt.*;

/**
 * Created by patrick on 11/9/16.
 */
public interface RenderStrategy {
    void render(GameModel model, Graphics2D graphics, int width, int height);
}
