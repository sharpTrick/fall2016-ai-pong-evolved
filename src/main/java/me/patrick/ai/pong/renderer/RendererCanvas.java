package me.patrick.ai.pong.renderer;

import me.patrick.ai.pong.model.GameModel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by patrick on 11/9/16.
 */
public class RendererCanvas extends Canvas implements GameRenderer{
    private final RenderStrategy renderStrategy;
    private volatile GameModel model = null;
    private volatile boolean changed = false;
    private final Lock lock = new ReentrantLock();
    private final Timer renderTimer = new Timer(5,(e)-> render());

    public RendererCanvas(RenderStrategy renderStrategy) {
        this.renderStrategy = renderStrategy;
        setIgnoreRepaint(true);
        setPreferredSize(new Dimension(500,500));
    }

    private void render() {
        if(changed) {
            try {
                lock.lock();
                BufferStrategy strategy = getBufferStrategy();
                Graphics graphics = strategy.getDrawGraphics();
                renderStrategy.render(model, (Graphics2D) graphics, getWidth(), getHeight());
                graphics.dispose();
                strategy.show();
                Toolkit.getDefaultToolkit().sync();
                changed = false;
            } finally {
                lock.unlock();
            }
        }
    }


    public void render(GameModel model) {
        if(!changed){
            if(lock.tryLock()){try {
                this.model=model.clone();
                changed = true;
            }finally {
                lock.unlock();
            }}
        }
    }

    public void startRendering(){
        renderTimer.start();
    }
    public void stopRendering(){
        renderTimer.stop();
    }
}
