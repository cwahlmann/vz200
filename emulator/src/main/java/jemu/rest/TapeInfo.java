package jemu.rest;

public class TapeInfo {
    public enum State {
        stopped, reading, writing
    }
    private String name;
    private int position;
    private int positionCount;
    private State state;

    public TapeInfo(String name, int position, int positionCount, State state) {
        this.name = name;
        this.position = position;
        this.positionCount = positionCount;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public State getState() {
        return state;
    }
}
