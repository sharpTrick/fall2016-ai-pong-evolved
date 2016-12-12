package me.patrick.ai.pong;

import me.patrick.ai.pong.model.GameModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by patrick on 12/12/16.
 */
public class ModelTest {

    @Test
    public void TestModel(){
        int balls = 5, paddles=4;

        GameModel model = new GameModel(5,4);
        Assert.assertTrue(model.getBallCount()==5);
        Assert.assertTrue(model.getPaddleCount()==4);

        for(int ball=0;ball<balls;ball++){
            for(int arg=0;arg<GameModel.BALL_ARG_COUNT; arg++){
                double originalValue = model.getBallValue(ball, arg);
                double random = Math.random();
                model.setBallValue(ball, arg, random);
                double newValue = model.getBallValue(ball, arg);
                Assert.assertFalse(originalValue==newValue);
                Assert.assertTrue(random==newValue);
            }
        }

        for(int paddle=0;paddle<paddles;paddle++){
            for(int arg=0;arg<GameModel.PADDLE_ARG_COUNT; arg++){
                double originalValue = model.getPaddleValue(paddle, arg);
                double random = Math.random();
                model.setPaddleValue(paddle, arg, random);
                double newValue = model.getPaddleValue(paddle, arg);
                Assert.assertFalse(originalValue==newValue);
                Assert.assertTrue(random==newValue);
            }
        }

        GameModel clone = model.clone();

        Assert.assertFalse(model==clone);
        Assert.assertTrue(model.getBallCount()==clone.getBallCount());
        Assert.assertTrue(model.getPaddleCount()==clone.getPaddleCount());
        for(int ball=0;ball<balls;ball++){
            for(int arg=0;arg<GameModel.BALL_ARG_COUNT; arg++){
                double originalValue = model.getBallValue(ball, arg);
                double newValue = clone.getBallValue(ball, arg);
                Assert.assertTrue(originalValue==newValue);
                double random = Math.random();
                clone.setBallValue(ball, arg, random);
                newValue = clone.getBallValue(ball, arg);
                Assert.assertFalse(originalValue==newValue);
                double doubleCheckOriginalValue = model.getBallValue(ball, arg);
                Assert.assertTrue(originalValue==doubleCheckOriginalValue);
            }
        }

        for(int paddle=0;paddle<paddles;paddle++){
            for(int arg=0;arg<GameModel.PADDLE_ARG_COUNT; arg++){
                double originalValue = model.getPaddleValue(paddle, arg);
                double newValue = clone.getPaddleValue(paddle, arg);
                Assert.assertTrue(originalValue==newValue);
                double random = Math.random();
                clone.setBallValue(paddle, arg, random);
                newValue = clone.getBallValue(paddle, arg);
                Assert.assertFalse(originalValue==newValue);
                double doubleCheckOriginalValue = model.getPaddleValue(paddle, arg);
                Assert.assertTrue(originalValue==doubleCheckOriginalValue);
            }
        }




    }
}
