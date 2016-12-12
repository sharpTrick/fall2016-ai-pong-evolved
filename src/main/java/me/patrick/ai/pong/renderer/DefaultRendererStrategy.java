package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.model.GameModel;

import java.awt.*;
import java.util.*;

/**
 * Created by patrick on 12/1/16.
 */
public class DefaultRendererStrategy implements RenderStrategy {
    private final java.util.List<RenderStrategy> rendererList = Arrays.asList(
            new CourtRenderer(),
            new IndexedRendererStrategy<>(FancyBallRenderer::new,GameModel::getBallCount),
            new IndexedRendererStrategy<>(PaddleRenderer::new,GameModel::getPaddleCount)
    );

    @Override
    public void render(GameModel model, Graphics2D graphics, int width, int height) {
        rendererList.forEach(renderer->{
            Graphics localGraphics = graphics.create();
            renderer.render(model, (Graphics2D) localGraphics, width, height);
            localGraphics.dispose();
        });
    }


}
