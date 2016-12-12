package me.patrick.ai.pong.controller;

/**
 * Created by patrick on 11/9/16.
 */
public class Input {
    private final int paddleIndex;
    private final double direction;

    public Input(int paddleIndex, double direction) {
        this.paddleIndex = paddleIndex;
        this.direction = direction;
    }

    public int getPaddleIndex() {
        return paddleIndex;
    }

    public double getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Input input = (Input) o;

        return paddleIndex == input.paddleIndex;

    }

    @Override
    public int hashCode() {
        return paddleIndex;
    }

}
