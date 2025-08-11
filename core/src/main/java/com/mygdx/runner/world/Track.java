package com.mygdx.runner.world;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines start/finish and obstacles.
 */
public class Track {
    public static final float RACE_LENGTH_PX = 4200f;
    private final float startX = 0f;
    private final float finishX = startX + RACE_LENGTH_PX;
    private final List<Rectangle> obstacles = new ArrayList<>();
    private final float groundY = 0f;
    private final float npcMin = 160f;
    private final float npcMax = 205f;

    public Track() {
        generateObstacles();
    }

    private void generateObstacles() {
        // 5 obstacles spaced along the track, avoiding crowding the last third
        float[] xs = {900f, 1700f, 2400f, 3100f, 3600f};
        for (float x : xs) {
            obstacles.add(new Rectangle(x, groundY, 40f, 20f));
        }
    }

    public float getFinishX() { return finishX; }
    public List<Rectangle> getObstacles() { return obstacles; }
    public float getNpcMin() { return npcMin; }
    public float getNpcMax() { return npcMax; }
    public float getGroundY() { return groundY; }
}
