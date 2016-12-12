package me.patrick.ai.pong;


import me.patrick.ai.pong.ai.NeuralNetwork;
import me.patrick.ai.pong.controller.InputManager;
import me.patrick.ai.pong.engine.GameEngine;
import me.patrick.ai.pong.model.GameModel;
import me.patrick.ai.pong.renderer.DefaultRendererStrategy;
import me.patrick.ai.pong.renderer.RendererCanvas;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by patrick on 11/9/16.
 */
public class Main {
    public static boolean done=false;
    public static void main (String... args) {
        while (!done) {
            for(int i=1;!done && i<=1;i+=(i+1)/2) {
                playGames(playGames(i,4).collect(Collectors.toList()), 600000);

            }
        }
    }


    public static Stream<Pair<GameLoop, RendererCanvas>> playGames(int balls, int paddles){

        File file = new File("MyMultiLayerNetwork "+balls+"|"+1+".zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally

        MultiLayerNetwork mln = loadOrCreateNeuralNetwork(file, true);


        return IntStream.range(0, 4)
                .mapToObj(i -> getGame(mln, balls, paddles));
    }

    public static HashMap<File, MultiLayerNetwork> mlnMap = new HashMap<>();
    public static MultiLayerNetwork loadOrCreateNeuralNetwork(File file, boolean saveWhenExit){
        MultiLayerNetwork multiLayerNetwork = mlnMap.computeIfAbsent(file, k -> {
            MultiLayerNetwork mln = null;

            if (file.exists()) {
                //Load the model
                try {
                    mln = ModelSerializer.restoreMultiLayerNetwork(file);
                    System.out.println("NeuralNetwork Loaded!!!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mln == null) {
                mln = NeuralNetwork.buildNetwork(1);
                System.out.println("NeuralNetwork Initialized!!!");
            }

            if (saveWhenExit) {
                final MultiLayerNetwork finalMLN = mln;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    //Save the model
                    try {
                        ModelSerializer.writeModel(finalMLN, file, true);
                        System.out.println("NeuralNetwork Saved!!!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "Save NeuralNetwork"));
            }

            return mln;
        });
        multiLayerNetwork.rnnClearPreviousState();
        return multiLayerNetwork;
    }

    public static Pair<GameLoop, RendererCanvas> getGame(MultiLayerNetwork mln, int balls, int paddles){

        GameModel model = new GameModel(balls,paddles);
        InputManager inputManager = () -> Collections.emptyList();
        RendererCanvas renderer = new RendererCanvas(new DefaultRendererStrategy());
        GameEngine engine = new GameEngine();

        GameLoop loop = new GameLoop(model, inputManager, engine, renderer, mln);

        return new ImmutablePair<>(loop, renderer);
    }

    public static void playGames(List<Pair<GameLoop, RendererCanvas>> games, int time){
        Lock waitLock = new ReentrantLock();
        Condition condition = waitLock.newCondition();
        try {
            waitLock.lock();
            SwingUtilities.invokeLater(()-> {
                JPanel contentPane = new JPanel();
                contentPane.setLayout(new BorderLayout());

                JPanel gamePanel = new JPanel();
                gamePanel.setLayout(new GridLayout(0, (int)Math.min(Math.ceil(Math.sqrt(games.size())),3)));
                contentPane.add(gamePanel,BorderLayout.CENTER);
                games.stream().map(Pair::getRight).forEach(renderer->gamePanel.add(renderer));

                JSlider slider = new JSlider(1,101);
                slider.setValue(games.get(0).getLeft().getGameSpeed());
                contentPane.add(slider, BorderLayout.SOUTH);
                slider.addChangeListener(e ->
                    games.stream().map(Pair::getLeft)
                            .forEach(loop->loop.setGameSpeed(slider.getValue()))
                );



                JFrame frame = new JFrame();
                frame.setTitle("THE GAME");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(contentPane);
                frame.pack();
                frame.setResizable(false);
                frame.setVisible(true);


                AtomicInteger threadCounter = new AtomicInteger(1);
                List<Thread> gameLoops = games.stream().map(pair -> {
                    pair.getRight().createBufferStrategy(2);
                    pair.getRight().startRendering();
                    return new Thread(pair.getLeft(), "GameLoop: " + threadCounter.getAndIncrement());
                }).peek(Thread::start).collect(Collectors.toList());

                Timer timer = new Timer(time, (evt) -> {
                    games.forEach(pair->{
                        pair.getLeft().kill();
                        pair.getRight().stopRendering();
                    });
                    gameLoops.forEach((thread) -> {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    frame.dispose();
                    try{
                        waitLock.lock();
                        condition.signalAll();
                    }finally {
                        waitLock.unlock();
                    }
                });
                timer.setRepeats(false);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        timer.stop();
                        games.forEach(pair->{
                            pair.getLeft().kill();
                            pair.getRight().stopRendering();
                        });
                        try{
                            waitLock.lock();
                            condition.signalAll();
                        }finally {
                            waitLock.unlock();
                        }
                        done=true;
                    }
                });
                timer.start();

            });

            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            waitLock.unlock();
        }
    }
}