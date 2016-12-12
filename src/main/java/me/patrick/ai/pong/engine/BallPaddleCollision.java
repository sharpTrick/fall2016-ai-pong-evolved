package me.patrick.ai.pong.engine;

import me.patrick.ai.pong.model.GameModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.stream.IntStream;

/**
 * Created by patrick on 12/6/16.
 */
public class BallPaddleCollision {
    public static void handleBallPaddleCollisions(GameModel model){
        IntStream.range(0, model.getBallCount()).forEach(ball->{
            double ballRadius = GameModel.BALL_RADIUS;
            double ballX = model.getBallValue(ball, GameModel.BALL_X);
            double ballY = model.getBallValue(ball, GameModel.BALL_Y);
            double ballVelX = model.getBallValue(ball, GameModel.BALL_VEL_X);
            double ballVelY = model.getBallValue(ball, GameModel.BALL_VEL_Y);
            IntStream.range(0, model.getPaddleCount()).forEach(paddle->{
                double paddleX = model.getPaddleValue(paddle,GameModel.PADDLE_X);
                double paddleY = model.getPaddleValue(paddle,GameModel.PADDLE_Y);
                double paddleHeight = model.getPaddleValue(paddle,GameModel.PADDLE_HEIGHT);
                double paddleWidth = model.getPaddleValue(paddle,GameModel.PADDLE_WIDTH);
                double diffX = ballX - paddleX;
                double diffY = ballY - paddleY;

                //check if ball is close
                if(!isBallClose(ballRadius, paddleWidth, paddleHeight, diffX, diffY)){
                    return;
                }
                //double check for collision
//                if(!isCollision(ballX, ballY, ballRadius, paddleX, paddleY, paddleWidth, paddleHeight, diffX, diffY)){
//                    return;
//                }

                Triple<Face, Double, Double> collisionData = getCollisionData(ballX, ballY, ballVelX, ballVelY, ballRadius, paddleX, paddleY, paddleWidth, paddleHeight);
                if(collisionData==null) return;

                Face faceStruck = collisionData.getLeft();
                double newX = collisionData.getMiddle();
                double newY = collisionData.getRight();

                double newVelX = ballVelX;
                double newVelY = ballVelY;

                //todo: figure out angular velocity stuff
                //todo: add paddle velocity to ball velocity
                switch (faceStruck) {
                    case Y0:
                        newVelY = Math.abs(ballVelY);
                        break;
                    case Y1:
                        newVelY = -Math.abs(ballVelY);
                        break;
                    case X0:
                        newVelX = Math.abs(ballVelX);
                        break;
                    case X1:
                        newVelX = -Math.abs(ballVelX);
                        break;
                }

                model.setBallValue(ball, GameModel.BALL_X, newX);
                model.setBallValue(ball, GameModel.BALL_Y, newY);
                model.setBallValue(ball, GameModel.BALL_VEL_X, newVelX);
                model.setBallValue(ball, GameModel.BALL_VEL_Y, newVelY);
            });
        });
    }

    public static boolean isBallClose(double ballRadius, double paddleWidth, double paddleHeight, double diffX, double diffY){
        double heightPlusRadius = ballRadius+paddleHeight/2;
        double widthPlusRadius = ballRadius+paddleWidth/2;

        return (Math.abs(diffY)<heightPlusRadius*0.999 && Math.abs(diffX)<widthPlusRadius*0.999);
    }



    public static Pair<Double,Double> getClosestPointToBall(double ballX, double ballY, double paddleX, double paddleY, double paddleWidth, double paddleHeight, double diffX, double diffY){
        double halfHeight = paddleHeight/2;
        double halfWidth = paddleWidth/2;

        //x and y are distances from paddle center
        double x = paddleX<ballX?halfWidth:-halfWidth;
        double y = x * diffY/diffX;
        if(halfHeight<Math.abs(y)){
            y=paddleY<ballY?halfHeight:-halfHeight;
            x = y * diffX/diffY;
        }
        return new ImmutablePair<>(paddleX+x, paddleY+y);
    }

    public static boolean isCollision(double ballX, double ballY, double ballRadius, double paddleX, double paddleY, double paddleWidth, double paddleHeight, double diffX, double diffY){
        Pair<Double, Double> closestPointToBall = getClosestPointToBall(ballX, ballY, paddleX, paddleY, paddleWidth, paddleHeight,diffX, diffY);
        double x = closestPointToBall.getLeft();
        double y = closestPointToBall.getRight();

        double distance = GameEngine.findDistance(
                x, y,
                ballX, ballY
        );

        return distance<ballRadius;
    }

    public static Triple<Face,Double,Double> getCollisionData(double ballX, double ballY, double ballVelX, double ballVelY, double ballRadius, double paddleX, double paddleY, double paddleWidth, double paddleHeight){
        double halfHeight = paddleHeight/2;
        double halfWidth = paddleWidth/2;

        double heightPlusRadius = halfHeight+ballRadius;
        double widthPlusRadius = halfWidth+ballRadius;



        Face face = 0<ballVelX?Face.X1 :Face.X0;

        double cX = paddleX + (0<ballVelX?-widthPlusRadius:widthPlusRadius);
        double cbX = cX - ballX;
        double cbY = cbX * ballVelY/ballVelX;
        double cY = cbY+ballY;

        //this should really be a distance test from point on on paddle closest to ball
        //but this will work (treats ball as if it's hitbox is square)
        if(heightPlusRadius<Math.abs(paddleY-cY)){
            face = 0<ballVelY?Face.Y1 :Face.Y0;
            cY = paddleY + (0<ballVelY?-heightPlusRadius:heightPlusRadius);
            cbY = cY - ballY;
            cbX = cbY * ballVelX/ballVelY;
            cX = cbX+ballX;
            if(widthPlusRadius<Math.abs(paddleX-cX)) return null;
        }

        return new ImmutableTriple<>(face, cX, cY);
    }

    private enum Face{
        Y0, Y1, X0, X1
    }
}
