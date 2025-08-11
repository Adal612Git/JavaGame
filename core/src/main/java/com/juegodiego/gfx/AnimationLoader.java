package com.juegodiego.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.juegodiego.personajes.Personaje.Estado;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;

/**
 * Carga animaciones para los personajes con diagnósticos detallados.
 */
public class AnimationLoader {
    private static final float RUN_SPEED = 0.06f;
    private static final float DEFAULT_SPEED = 0.1f;

    private AnimationLoader() {}

    /**
     * Carga animaciones para un personaje y registra diagnósticos.
     */
    public static EnumMap<Estado, Animation<TextureRegion>> loadFor(
            String personaje, AssetManager am, GdxDiagnostics diag) {
        EnumMap<Estado, Animation<TextureRegion>> map = new EnumMap<>(Estado.class);
        String base = "images/personajes/" + personaje + "/";

        // Búsqueda en estructura estándar
        for (Estado st : Estado.values()) {
            String dirPath = base + st.name().toLowerCase();
            FileHandle dir = Gdx.files.internal(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                FileHandle[] files = dir.list("png");
                if (files != null && files.length > 0) {
                    Array<FileHandle> matches = new Array<>();
                    String patternUsed = "";
                    if (st == Estado.RUN) {
                        String[] pats = {"run_\\d+\\.png", "Run_\\d+\\.png", "Gato\\d+\\.png", "Mapache\\d+\\.png", "Conejo\\d+\\.png"};
                        for (String p : pats) {
                            matches.clear();
                            for (FileHandle fh : files) {
                                if (fh.name().matches(p)) matches.add(fh);
                            }
                            if (matches.size > 0) { patternUsed = p; break; }
                        }
                    } else if (st == Estado.IDLE) {
                        FileHandle idle = dir.child("idle.png");
                        if (idle.exists()) {
                            matches.add(idle);
                            patternUsed = "idle.png";
                        } else {
                            for (FileHandle fh : files) {
                                if (fh.name().matches("idle_\\d+\\.png")) matches.add(fh);
                            }
                            if (matches.size > 0) patternUsed = "idle_####.png";
                        }
                    } else {
                        for (FileHandle fh : files) {
                            matches.add(fh);
                        }
                        if (matches.size > 0) patternUsed = "*";
                    }

                    if (matches.size > 0) {
                        matches.sort(Comparator.comparing(FileHandle::name));
                        Array<TextureRegion> frames = new Array<>();
                        for (FileHandle fh : matches) {
                            String path = dirPath + "/" + fh.name();
                            am.load(path, Texture.class);
                            am.finishLoadingAsset(path);
                            Texture tex = am.get(path, Texture.class);
                            frames.add(new TextureRegion(tex));
                        }
                        float fd = st == Estado.RUN ? RUN_SPEED : DEFAULT_SPEED;
                        map.put(st, new Animation<>(fd, frames));
                        String sample = dirPath + "/" + matches.first().name();
                        Gdx.app.log("[" + personaje + "]", "FOUND " + st + " " + matches.size + " frames @ " + dirPath + " pattern=" + patternUsed);
                        diag.record(personaje, st, "FOUND", matches.size, sample);
                        continue;
                    }
                }
            }
            Gdx.app.log("[" + personaje + "]", "MISSING " + st + " @ " + dirPath);
        }

        // Mapeo de animal para fallbacks
        String jumpFile, deathFile, runFolder;
        switch (personaje.toLowerCase()) {
            case "roky":
                jumpFile = "Racoon_jump.png";
                deathFile = "Mapache_Skin.png";
                runFolder = "Mapache_Run";
                break;
            case "thumper":
                jumpFile = "Rabbit_jump.png";
                deathFile = "Conejo_Skin.png";
                runFolder = "Conejo_Run";
                break;
            case "orion":
            default:
                jumpFile = "Cat_jump.png";
                deathFile = "Gato_Skin.png";
                runFolder = "Gato_Run";
                break;
        }
        String speedBase = "images/personajes/animaciones/Speedpaws_Char/";

        // Fallback RUN
        if (!map.containsKey(Estado.RUN)) {
            String fbDir = speedBase + runFolder + "/";
            FileHandle dir = Gdx.files.internal(fbDir);
            if (dir.exists()) {
                FileHandle[] files = dir.list("png");
                if (files != null && files.length > 0) {
                    Arrays.sort(files, Comparator.comparing(FileHandle::name));
                    Array<TextureRegion> frames = new Array<>();
                    for (FileHandle fh : files) {
                        String path = fbDir + fh.name();
                        am.load(path, Texture.class);
                        am.finishLoadingAsset(path);
                        frames.add(new TextureRegion(am.get(path, Texture.class)));
                    }
                    map.put(Estado.RUN, new Animation<>(RUN_SPEED, frames));
                    diag.record(personaje, Estado.RUN, "FALLBACK", files.length, fbDir + files[0].name());
                    Gdx.app.log("[" + personaje + "]", "FALLBACK RUN " + files.length + " frames @ " + fbDir);
                } else {
                    Gdx.app.log("[" + personaje + "]", "MISSING RUN @ " + fbDir);
                }
            } else {
                Gdx.app.log("[" + personaje + "]", "MISSING RUN @ " + fbDir);
            }
        }

        // Fallback IDLE desde RUN
        if (!map.containsKey(Estado.IDLE) && map.containsKey(Estado.RUN)) {
            TextureRegion first = map.get(Estado.RUN).getKeyFrames()[0];
            map.put(Estado.IDLE, new Animation<>(DEFAULT_SPEED, first));
            diag.record(personaje, Estado.IDLE, "FALLBACK", 1, "from RUN");
            Gdx.app.log("[" + personaje + "]", "FALLBACK IDLE 1 frames @ from RUN");
        }

        // Fallback JUMP
        if (!map.containsKey(Estado.JUMP)) {
            String path = speedBase + "Jump_pose/" + jumpFile;
            FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                TextureRegion region = new TextureRegion(am.get(path, Texture.class));
                map.put(Estado.JUMP, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.JUMP, "FALLBACK", 1, path);
                Gdx.app.log("[" + personaje + "]", "FALLBACK JUMP 1 frames @ " + path);
            } else {
                Gdx.app.log("[" + personaje + "]", "MISSING JUMP @ " + path);
            }
        }

        // Fallback FALL
        if (!map.containsKey(Estado.FALL)) {
            String path = speedBase + "Jump_pose/" + jumpFile;
            FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                TextureRegion region = new TextureRegion(am.get(path, Texture.class));
                map.put(Estado.FALL, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.FALL, "FALLBACK", 1, path);
                Gdx.app.log("[" + personaje + "]", "FALLBACK FALL 1 frames @ " + path);
            } else {
                Gdx.app.log("[" + personaje + "]", "MISSING FALL @ " + path);
            }
        }

        // Fallback ATTACK y HURT desde IDLE
        if (!map.containsKey(Estado.ATTACK) && map.containsKey(Estado.IDLE)) {
            map.put(Estado.ATTACK, map.get(Estado.IDLE));
            diag.record(personaje, Estado.ATTACK, "FALLBACK", 1, "from IDLE");
            Gdx.app.log("[" + personaje + "]", "FALLBACK ATTACK 1 frames @ from IDLE");
        }
        if (!map.containsKey(Estado.HURT) && map.containsKey(Estado.IDLE)) {
            map.put(Estado.HURT, map.get(Estado.IDLE));
            diag.record(personaje, Estado.HURT, "FALLBACK", 1, "from IDLE");
            Gdx.app.log("[" + personaje + "]", "FALLBACK HURT 1 frames @ from IDLE");
        }

        // Fallback DEAD
        if (!map.containsKey(Estado.DEAD)) {
            String path = speedBase + "Death/" + deathFile;
            FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                TextureRegion region = new TextureRegion(am.get(path, Texture.class));
                map.put(Estado.DEAD, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.DEAD, "FALLBACK", 1, path);
                Gdx.app.log("[" + personaje + "]", "FALLBACK DEAD 1 frames @ " + path);
            } else {
                Gdx.app.log("[" + personaje + "]", "MISSING DEAD @ " + path);
            }
        }

        // Final fallback
        for (Estado st : Estado.values()) {
            if (!map.containsKey(st)) {
                Color color;
                switch (st) {
                    case RUN: color = Color.BLUE; break;
                    case JUMP: color = Color.YELLOW; break;
                    case FALL: color = Color.ORANGE; break;
                    case ATTACK: color = Color.RED; break;
                    case HURT: color = Color.PURPLE; break;
                    case DEAD: color = Color.GRAY; break;
                    case IDLE:
                    default: color = Color.GREEN; break;
                }
                Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pm.setColor(color);
                pm.fill();
                Texture tex = new Texture(pm);
                pm.dispose();
                TextureRegion region = new TextureRegion(tex);
                map.put(st, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, st, "FINAL_FALLBACK", 1, "solid-color");
                Gdx.app.log("[" + personaje + "]", "FINAL_FALLBACK " + st + " solid-color");
            }
        }

        return map;
    }
}

