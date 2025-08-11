package com.mygdx.runner.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.mygdx.runner.characters.State;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;

/**
 * Loads animations for a character from disk following the required
 * directory structure.
 */
public class AnimationLoader {
    public static class Result {
        public final EnumMap<State, Animation<TextureRegion>> animations = new EnumMap<>(State.class);
        public final Array<Texture> textures = new Array<>();
        public final EnumMap<State, Integer> counts = new EnumMap<>(State.class);
        public boolean runUsedIdle = false;
    }

    private AnimationLoader() {}

    /** Loads animations for the given character id. */
    public static Result load(String id) {
        Result res = new Result();
        for (State st : State.values()) {
            String dirPath = "assets/images/personajes/" + id + "/" + st.name().toLowerCase();
            FileHandle dir = Gdx.files.internal(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                FileHandle[] files = dir.list("png");
                Arrays.sort(files, Comparator.comparing(FileHandle::name));
                if (files.length > 0) {
                    Array<TextureRegion> frames = new Array<>();
                    for (FileHandle f : files) {
                        Texture tex = new Texture(f);
                        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                        res.textures.add(tex);
                        frames.add(new TextureRegion(tex));
                    }
                    float fd = st == State.RUN ? 0.07f : 0.1f;
                    Animation<TextureRegion> anim = new Animation<>(fd, frames.toArray(TextureRegion.class));
                    anim.setPlayMode(Animation.PlayMode.LOOP);
                    res.animations.put(st, anim);
                    res.counts.put(st, files.length);
                }
            }
        }
        // run fallback
        if (!res.animations.containsKey(State.RUN)) {
            Animation<TextureRegion> idle = res.animations.get(State.IDLE);
            if (idle != null) {
                Animation<TextureRegion> runAnim = new Animation<>(0.07f, idle.getKeyFrames());
                runAnim.setPlayMode(Animation.PlayMode.LOOP);
                res.animations.put(State.RUN, runAnim);
                res.counts.put(State.RUN, 0);
                res.runUsedIdle = true;
                Gdx.app.log("WARN", "Missing RUN frames for " + id + ". Using IDLE frames.");
            }
        }
        // placeholder fallback
        if (res.animations.isEmpty()) {
            FileHandle ph = Gdx.files.internal("assets/images/personajes/" + id + "/placeholder.png");
            if (ph.exists()) {
                Texture tex = new Texture(ph);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                res.textures.add(tex);
                TextureRegion region = new TextureRegion(tex);
                Animation<TextureRegion> anim = new Animation<>(0.1f, region);
                anim.setPlayMode(Animation.PlayMode.LOOP);
                res.animations.put(State.IDLE, anim);
                res.animations.put(State.RUN, anim);
                res.counts.put(State.IDLE, 1);
                res.counts.put(State.RUN, 1);
                Gdx.app.log("WARN", "Using placeholder for character " + id);
            } else {
                Gdx.app.log("WARN", "No images found for character " + id);
            }
        }
        // summary log
        StringBuilder sb = new StringBuilder("Personaje " + id + ": ");
        for (State st : State.values()) {
            Integer c = res.counts.get(st);
            if (c != null) {
                sb.append(st.name().toLowerCase()).append("(").append(c);
                if (st == State.RUN && res.runUsedIdle) sb.append("->fallback idle");
                sb.append(") ");
            }
        }
        Gdx.app.log("INFO", sb.toString().trim());
        return res;
    }
}
