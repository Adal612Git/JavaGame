package com.mygdx.runner.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines start/finish and obstacles.
 */
public class Track {
    private float startX = 0f;
    private float finishX = 3000f;
    private final List<Rectangle> obstacles = new ArrayList<>();
    private float groundY = 0f;
    private float npcMin = 160f;
    private float npcMax = 210f;

    public Track() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            FileHandle fh = Gdx.files.internal("config/race_config.json");
            if (!fh.exists()) return;
            Json json = new Json();
            JsonValue root = json.fromJson(null, fh);
            finishX = root.getFloat("finishX", finishX);
            npcMin = root.getFloat("npcSpeedMin", npcMin);
            npcMax = root.getFloat("npcSpeedMax", npcMax);
            JsonValue obs = root.get("obstacles");
            if (obs != null) {
                for (JsonValue o : obs) {
                    float x = o.getFloat("x");
                    float w = o.getFloat("width");
                    float h = o.getFloat("height");
                    obstacles.add(new Rectangle(x, groundY, w, h));
                }
            }
        } catch (Exception e) {
            Gdx.app.error("Track", "Failed to load config", e);
        }
    }

    public float getFinishX() { return finishX; }
    public List<Rectangle> getObstacles() { return obstacles; }
    public float getNpcMin() { return npcMin; }
    public float getNpcMax() { return npcMax; }
    public float getGroundY() { return groundY; }
}
