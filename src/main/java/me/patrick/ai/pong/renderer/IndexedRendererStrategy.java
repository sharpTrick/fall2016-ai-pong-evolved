package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.util.HasIndex;
import me.patrick.ai.pong.model.GameModel;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by patrick on 12/2/16.
 */
public class IndexedRendererStrategy<R extends RenderStrategy & HasIndex> implements RenderStrategy{
    private final Supplier<R> supplier;
    private final Function<GameModel, Integer> endIndexFunction;

    public IndexedRendererStrategy(Supplier<R> supplier, Function<GameModel, Integer> endIndexFunction) {
        this.supplier = supplier;
        this.endIndexFunction = endIndexFunction;
    }

    @Override
    public void render(GameModel model, Graphics2D graphics, int width, int height) {
        R renderer = supplier.get();
        Integer endIndex = endIndexFunction.apply(model);
        for(int index = 0; index<endIndex; index++){
            renderer.setIndex(index);
            Graphics localGraphics = graphics.create();
            renderer.render(model, (Graphics2D) localGraphics, width, height);
            localGraphics.dispose();
        }
    }
}
