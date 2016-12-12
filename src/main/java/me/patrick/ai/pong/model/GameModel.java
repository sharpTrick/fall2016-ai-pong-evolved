package me.patrick.ai.pong.model;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by patrick on 11/9/16.
 */
public class GameModel implements Cloneable{
    public static final double INITIAL_BALL_VEL=0.1;
    public static final double PADDLE_LONG_DIM = 0.1;
    public static final double PADDLE_SHORT_DIM = 0.0125;

    public static final int PADDLE_ARG_COUNT = 6;
    public static final int BALL_ARG_COUNT = 4;


    public static final int PADDLE_HEIGHT =0;
    public static final int PADDLE_WIDTH=1;
    public static final int PADDLE_X=2;
    public static final int PADDLE_Y=3;
    public static final int PADDLE_VEL_X=4;
    public static final int PADDLE_VEL_Y=5;
    //public static final int PADDLE_ROT=4;
//    public static final int PADDLE_VEL_ROT=7;

    public static final double BALL_RADIUS =0.03;
    public static final int BALL_X=0;
    public static final int BALL_VEL_X=1;
    public static final int BALL_Y=2;
    public static final int BALL_VEL_Y=3;
//    public static final int BALL_ROT=3;
//    public static final int BALL_VEL_ROT=6;

    //////////////////////////////////////////////////////

    private final int ballCount;
    private final int paddleCount;
    private final double[] array;

    private final int startBallIndex;

    /////////////////////////////////////////////////////


    public GameModel() {
        this(1,2);
    }
    public GameModel(int ballCount, int paddleCount) {
        this.ballCount = ballCount;
        this.paddleCount = paddleCount;
        array = new double[BALL_ARG_COUNT *ballCount+PADDLE_ARG_COUNT*paddleCount];
        startBallIndex = paddleCount*PADDLE_ARG_COUNT;

        reInit();
    }

    private GameModel(GameModel model){
        this.ballCount = model.ballCount;
        this.paddleCount = model.paddleCount;
        this.array = Arrays.copyOf(model.array,model.array.length);
        this.startBallIndex = model.startBallIndex;
    }

    public void reInit(){
        for(int paddle = 0; paddle<paddleCount; paddle++) {
            reInitPaddle(paddle);
        }
        for(int ball = 0; ball<ballCount; ball++) {
            reInitBall(ball);
        }
    }

    public void reInitPaddle(int paddle){
        PADDLE_TYPE paddleType = getPaddleType(paddle);

        double halfHeight = paddleType==PADDLE_TYPE.VERTICAL?PADDLE_LONG_DIM:PADDLE_SHORT_DIM;
        double halfWidth  = paddleType==PADDLE_TYPE.VERTICAL?PADDLE_SHORT_DIM:PADDLE_LONG_DIM;

        setPaddleValue(paddle, PADDLE_HEIGHT, halfHeight*2);
        setPaddleValue(paddle, PADDLE_WIDTH, halfWidth*2);
        //setPaddleValue(paddle, PADDLE_ROT, (paddle/2)%2==0?0:0.25);
        setPaddleValue(paddle, PADDLE_VEL_X, 0);
        setPaddleValue(paddle, PADDLE_VEL_Y, 0);
//        setPaddleValue(paddle, PADDLE_VEL_ROT, 0);

        switch (paddleType){
            case VERTICAL:
                setPaddleValue(paddle, PADDLE_X, paddle%2==0?(halfWidth):(1-halfWidth));
                setPaddleValue(paddle, PADDLE_Y, 0.5);
                break;
            case HORIZONTAL:
                setPaddleValue(paddle, PADDLE_X, 0.5);
                setPaddleValue(paddle, PADDLE_Y, paddle%2==0?(halfHeight):(1-halfHeight));
                break;
        }
    }

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();
    public void reInitBall(int ball){
        double randomAngle = Math.random() * Math.PI*2;
        randomAngle = Math.PI/4*(1*ATOMIC_INTEGER.getAndIncrement());

//        setBallValue(ball,BALL_RADIUS, 0.03);
        setBallValue(ball,BALL_X, 0.5);
        setBallValue(ball,BALL_Y, 0.5);
//        setBallValue(ball,BALL_ROT, Math.random()-0.5);
        setBallValue(ball,BALL_VEL_X, INITIAL_BALL_VEL * Math.sin(randomAngle));
        setBallValue(ball,BALL_VEL_Y, INITIAL_BALL_VEL * Math.cos(randomAngle));
//        setBallValue(ball,BALL_VEL_ROT, Math.random()*2-0.5);
    }



    //////////////////////////////////////////////////////

    public int getPaddleCount() {
        return paddleCount;
    }

    public int getBallCount() {
        return ballCount;
    }

    public int getTotalArgCount(){
        return BALL_ARG_COUNT *ballCount+PADDLE_ARG_COUNT*paddleCount;
    }

    public int getStartBallIndex() {
        return startBallIndex;
    }

    public double[] getRef(){
        return array;
    }

    public double getBallValue(int ball, int valueType){
        return array[startBallIndex+ball* BALL_ARG_COUNT +valueType];
    }

    public void setBallValue(int ball, int valueType, double value){
        array[startBallIndex+ball* BALL_ARG_COUNT +valueType] = value;
    }

    public double getPaddleValue(int paddle, int valueType){
        return array[paddle*PADDLE_ARG_COUNT+valueType];
    }


    public double getPaddleLocation(int paddle){
        PADDLE_TYPE paddleType = getPaddleType(paddle);
        int valueType = paddleType==PADDLE_TYPE.VERTICAL?PADDLE_Y:PADDLE_X;
        return getPaddleValue(paddle, valueType);
    }

    public void setPaddleValue(int paddle, int valueType, double value){
        array[paddle*PADDLE_ARG_COUNT+valueType]=value;
    }

    public void setPaddleVel(int paddle, double value){
        PADDLE_TYPE paddleType = getPaddleType(paddle);
        int valueType = paddleType==PADDLE_TYPE.VERTICAL?PADDLE_VEL_Y:PADDLE_VEL_X;
        setPaddleValue(paddle, valueType,value);
    }



    @Override
    public GameModel clone() {
        return new GameModel(this);
    }

    ////////////////////////////////////////////////////////////////

    public static PADDLE_TYPE getPaddleType(int paddleIndex){
        return ((paddleIndex/2)%2==0?PADDLE_TYPE.VERTICAL:PADDLE_TYPE.HORIZONTAL);
    }
    public enum PADDLE_TYPE{
        VERTICAL,
        HORIZONTAL
    }
}
