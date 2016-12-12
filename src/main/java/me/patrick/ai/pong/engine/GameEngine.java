package me.patrick.ai.pong.engine;

import me.patrick.ai.pong.controller.Input;
import me.patrick.ai.pong.model.GameModel;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by patrick on 11/9/16.
 */
public class GameEngine {
    private static final double PADDLE_VELOCITY = GameModel.INITIAL_BALL_VEL;


    public Stream<? extends Triple<Integer, Integer, Double>> update(GameModel model, Collection<Input> inputList, Duration duration){

        double seconds = duration.toMillis()/1000d;
        processInput(model, inputList);

        Stream.<Runnable>of(
                ()->movePaddles(model, seconds),
                ()->moveBalls(model,seconds)
        )/*.parallel()*/.forEach(Runnable::run);

        handleBallBallCollisions(model);
//        BallPaddleCollision.handleBallPaddleCollisions(model);
        return getBallOffCanvas(model).peek(triple->{
            int valueType=0;
            int mult = 0;
            switch (triple.getMiddle()){
                case 0:
                case 1:
                    valueType = GameModel.BALL_VEL_X;
                    break;
                case 2:
                case 3:
                    valueType = GameModel.BALL_VEL_Y;
            }

            switch (triple.getMiddle()){
                case 0:
                case 2:
                    mult=1;
                    break;
                case 1:
                case 3:
                    mult=-1;
            }

            double vel = model.getBallValue(triple.getLeft(), valueType);
            model.setBallValue(triple.getLeft(),valueType,mult*Math.abs(vel));

        });
    }

    public void processInput(GameModel model, Collection<Input> inputList) {
        inputList.stream().unordered()/*.parallel()*/.distinct().forEach(input-> {
            int valueType = 0;
            switch (GameModel.getPaddleType(input.getPaddleIndex())) {
                case VERTICAL:
                    valueType = GameModel.PADDLE_VEL_Y;
                    break;
                case HORIZONTAL:
                    valueType = GameModel.PADDLE_VEL_X;
                    break;
            }
            model.setPaddleValue(input.getPaddleIndex(), valueType, PADDLE_VELOCITY*input.getDirection());
        });
    }

    private void movePaddles(GameModel model, double seconds){
        IntStream.range(0,model.getPaddleCount())/*.parallel()*/.forEach(paddle->{
            switch (GameModel.getPaddleType(paddle)){
                case VERTICAL:
                    model.setPaddleValue(
                            paddle,
                            GameModel.PADDLE_Y,
                            seconds * model.getPaddleValue(
                                    paddle,
                                    GameModel.PADDLE_VEL_Y
                            ) + model.getPaddleValue(
                                    paddle,
                                    GameModel.PADDLE_Y
                            )
                    );
                    if(model.getPaddleValue(paddle, GameModel.PADDLE_Y)<0){
                        model.setPaddleValue(paddle, GameModel.PADDLE_Y, 0);
                        model.setPaddleValue(paddle, GameModel.PADDLE_VEL_Y, 0);
                    }else if(1<model.getPaddleValue(paddle, GameModel.PADDLE_Y)){
                        model.setPaddleValue(paddle, GameModel.PADDLE_Y, 1);
                        model.setPaddleValue(paddle, GameModel.PADDLE_VEL_Y, 0);
                    }
                    break;
                case HORIZONTAL:
                    model.setPaddleValue(
                            paddle,
                            GameModel.PADDLE_X,
                            seconds * model.getPaddleValue(
                                    paddle,
                                    GameModel.PADDLE_VEL_X
                            ) + model.getPaddleValue(
                                    paddle,
                                    GameModel.PADDLE_X
                            )
                    );
                    if(model.getPaddleValue(paddle, GameModel.PADDLE_X)<0){
                        model.setPaddleValue(paddle, GameModel.PADDLE_X, 0);
                        model.setPaddleValue(paddle, GameModel.PADDLE_VEL_X, 0);
                    }else if(1<model.getPaddleValue(paddle, GameModel.PADDLE_X)){
                        model.setPaddleValue(paddle, GameModel.PADDLE_X, 1);
                        model.setPaddleValue(paddle, GameModel.PADDLE_VEL_X, 0);
                    }
                    break;
            }
        });
    }


    private void moveBalls(GameModel model, double seconds) {
        IntStream.range(0, model.getBallCount())/*.parallel()*/.forEach(ball -> {
            moveBall(model, ball, seconds);
        });
    }
    private void moveBall(GameModel model, int ball, double seconds){
        model.setBallValue(
                ball,
                GameModel.BALL_X,
                seconds * model.getBallValue(
                        ball,
                        GameModel.BALL_VEL_X
                ) + model.getBallValue(
                        ball,
                        GameModel.BALL_X
                )
        );
        model.setBallValue(
                ball,
                GameModel.BALL_Y,
                seconds * model.getBallValue(
                        ball,
                        GameModel.BALL_VEL_Y
                ) + model.getBallValue(
                        ball,
                        GameModel.BALL_Y
                )
        );
//        model.setBallValue(
//                ball,
//                GameModel.BALL_ROT,
//                seconds * model.getBallValue(
//                        ball,
//                        GameModel.BALL_VEL_ROT
//                ) + model.getBallValue(
//                        ball,
//                        GameModel.BALL_ROT
//                )
//        );
    }
    public static double findDistance(double fromX, double fromY, double toX, double toY){
        double a = Math.abs(fromX - toX);
        double b = Math.abs(fromY - toY);

        return Math.sqrt((a * a) + (b * b));
    }




    private void handleBallBallCollisions(GameModel model){
        IntStream.range(0, model.getBallCount()).forEach(ballA->{
            IntStream.range(ballA+1, model.getBallCount()).forEach(ballB->{
                double aX = model.getBallValue(ballA, GameModel.BALL_X);
                double aY = model.getBallValue(ballA, GameModel.BALL_Y);
//                double aR = model.getBallValue(ballA, GameModel.BALL_RADIUS);
                double bX = model.getBallValue(ballB, GameModel.BALL_X);
                double bY = model.getBallValue(ballB, GameModel.BALL_Y);
//                double bR = model.getBallValue(ballB, GameModel.BALL_RADIUS);
                double distance = findDistance(aX, aY, bX, bY);
                if(distance==0) return;
                if(distance<GameModel.BALL_RADIUS*2){
                    double collisionX = (aX-bX)/distance;
                    double collisionY = (aY-bY)/distance;
                    if(collisionX==0 || collisionY==0) return;

                    double aVX = model.getBallValue(ballA, GameModel.BALL_VEL_X);
                    double aVY = model.getBallValue(ballA, GameModel.BALL_VEL_Y);
                    double bVX = model.getBallValue(ballB, GameModel.BALL_VEL_X);
                    double bVY = model.getBallValue(ballB, GameModel.BALL_VEL_Y);

                    // Get the components of the velocity vectors which are parallel to the collision.
                    // The perpendicular component remains the same for both fish
                    double aci = aVX*collisionX+aVY*collisionY;
                    double bci = bVX*collisionX+bVY*collisionY;

                    // Solve for the new velocities using the 1-dimensional elastic collision equations.
                    // Turns out it's really simple when the masses are the same.
                    double acf = bci;
                    double bcf = aci;

                    // Replace the collision velocity components with the new ones
                    double newAVX = aVX + (acf - aci) * collisionX;
                    double newAVY = aVY + (acf - aci) * collisionY;
                    double newBVX = bVX + (bcf - bci) * collisionX;
                    double newBVY = bVY + (bcf - bci) * collisionY;

                    //todo: take into account

                    model.setBallValue(ballA, GameModel.BALL_VEL_X, newAVX);
                    model.setBallValue(ballA, GameModel.BALL_VEL_Y, newAVY);
                    model.setBallValue(ballB, GameModel.BALL_VEL_X, newBVX);
                    model.setBallValue(ballB, GameModel.BALL_VEL_Y, newBVY);

                    //need to find rate of change in distance
                    double comboVX = newAVX-newBVX;
                    double comboVY = newAVY-newBVY;
                    double comboMagnatude = Math.sqrt(comboVX*comboVX+comboVY*comboVY);
                    double timeToApart = (GameModel.BALL_RADIUS*2-distance)/comboMagnatude;

//                    progress balls forward in time until distance = radius+radius
                    moveBall(model, ballA, timeToApart);
                    moveBall(model, ballB, timeToApart);
                }
            });
        });
    }

    private Stream<? extends Triple<Integer, Integer,Double>> getBallOffCanvas(GameModel model){
        return IntStream.range(0, model.getBallCount()).mapToObj(ball->{
            int paddleCount = model.getPaddleCount();
            double x = model.getBallValue(ball, GameModel.BALL_X);
            double y = model.getBallValue(ball, GameModel.BALL_Y);
            if(x<0){
                return new ImmutableTriple<>(ball,0,y);
            }else if(1<x){
                if(paddleCount<2){
                    double vel = model.getBallValue(ball, GameModel.BALL_VEL_X);
                    model.setBallValue(ball,GameModel.BALL_VEL_X,-Math.abs(vel));
                }else return new ImmutableTriple<>(ball,1,y);
            }else if(y<0){
                if(paddleCount<3){
                    double vel = model.getBallValue(ball, GameModel.BALL_VEL_Y);
                    model.setBallValue(ball,GameModel.BALL_VEL_Y,Math.abs(vel));
                }else return new ImmutableTriple<>(ball,2,x);
            }else if(1<y){
                if(paddleCount<4){
                    double vel = model.getBallValue(ball, GameModel.BALL_VEL_Y);
                    model.setBallValue(ball,GameModel.BALL_VEL_Y,-Math.abs(vel));
                }else return new ImmutableTriple<>(ball,3,x);
            }
            return null;
        }).filter(Objects::nonNull);
    }
}
