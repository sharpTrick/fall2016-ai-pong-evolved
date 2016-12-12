package me.patrick.ai.pong;

import com.sun.org.apache.xpath.internal.SourceTree;
import me.patrick.ai.pong.ai.NeuralNetwork;
import me.patrick.ai.pong.controller.Input;
import me.patrick.ai.pong.controller.InputManager;
import me.patrick.ai.pong.engine.GameEngine;
import me.patrick.ai.pong.model.GameModel;
import me.patrick.ai.pong.renderer.GameRenderer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by patrick on 12/1/16.
 */
public class GameLoop implements Runnable {
    private final GameModel model;
    private final InputManager inputManager;
    private final GameEngine engine;
    private final GameRenderer renderer;
    private final List<NeuralNetwork> nns = new ArrayList<>();

    boolean done = false;

    public void setDone(boolean done) {
        this.done = done;
    }

    public void kill(){
        setDone(true);
    }

    private int gameSpeed = 50;

    public GameLoop(GameModel model, InputManager inputManager, GameEngine engine, GameRenderer renderer, MultiLayerNetwork mln) {
        this.model = model;
        this.inputManager = inputManager;
        this.engine = engine;
        this.renderer = renderer;

        int paddleCount = model.getPaddleCount();
        for(int paddle=0; paddle<paddleCount; paddle++){
            nns.add(new NeuralNetwork(mln, 1, paddle));
        }
    }

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    @Override
    public void run() {
        int paddleCount = model.getPaddleCount();
        while (!done){

            nns.forEach(nn->nn.logModel(model));
            double[] neuralValues = nns.stream()
                    .mapToDouble(nn->nn.getValues(model)[0])
                    .toArray();


            List<Input> inputs = IntStream.range(0, paddleCount)
                    .mapToObj(i ->{
                        //if(Math.abs(diff)<0.001) return new Input(i,0);
                        return new Input(i, neuralValues[i]);
                    }).collect(Collectors.toList());

//            nns.get(0).logDesiredPosition(0,0,0);
//            nns.get(1).logDesiredPosition(0,0,1);
//            nns.get(2).logDesiredPosition(0,0,0);
//            nns.get(3).logDesiredPosition(0,0,1);

//            System.out.println(Arrays.toString(neuralValues));

            engine.update(model,inputs, Duration.ofMillis(gameSpeed))
                    .forEach(triple->nns.get(triple.getMiddle())
                            .logDesiredPosition(
                                    triple.getLeft(),
                                    0,//triple.getMiddle(),
                                    triple.getRight()
                            )
                    );
            if(gameSpeed<101) renderer.render(model);
        }
    }
}
