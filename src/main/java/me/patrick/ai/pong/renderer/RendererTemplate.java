package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.model.GameModel;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by patrick on 12/1/16.
 */
public abstract class RendererTemplate implements RenderStrategy{
    abstract public double getX(GameModel model);
    abstract public double getY(GameModel model);
    abstract public double getWidth(GameModel model);
    abstract public double getHeight(GameModel model);
    abstract public double getRotate(GameModel model);
    abstract public void renderObject(Graphics2D graphics, int width, int height);

    @Override
    public void render(GameModel model, Graphics2D graphics, int canvasWidth, int canvasHeight) {
        int width = (int) (getWidth(model) * canvasWidth);
        int height = (int) (getHeight(model) * canvasHeight);

        // create the transform, note that the transformations happen
        // in reversed order (so check them backwards)
        AffineTransform at = new AffineTransform();

        // 4. translate it to the center of the component
        at.translate(getX(model)*canvasWidth, getY(model)*canvasHeight);

        // 3. do the actual rotation
        at.rotate(getRotate(model)*2*Math.PI);

        // 2. just a scale because this image is big
        //There will be no scaling

        // 1. translate the object so that you rotate it around the
        //    center (easier :))
        at.translate(-width/2, -height/2);

        // draw the image
        graphics.transform(at);

        renderObject(graphics, width, height);
    }



}
