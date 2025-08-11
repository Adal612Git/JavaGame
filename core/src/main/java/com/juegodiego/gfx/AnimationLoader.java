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
import com.badlogic.gdx.utils.ObjectMap;
import com.juegodiego.personajes.Personaje.Estado;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Carga animaciones de sprites para personajes.
 */
public class AnimationLoader {
    private static final float RUN_SPEED = 0.06f;
    private static final float DEFAULT_SPEED = 0.1f;

    public static ObjectMap<Estado, Animation<TextureRegion>> loadFor(String personajeName, AssetManager am) {
        ObjectMap<Estado, Animation<TextureRegion>> map = new ObjectMap<>();
        String base = "images/personajes/" + personajeName + "/";

        for (Estado st : Estado.values()) {
            String stateLower = st.name().toLowerCase();
            FileHandle dir = Gdx.files.internal(base + stateLower);
            if (dir.exists() && dir.isDirectory()) {
                FileHandle[] files = dir.list("png");
                if (files != null && files.length > 0) {
                    Arrays.sort(files, Comparator.comparing(FileHandle::name));
                    Array<TextureRegion> frames = new Array<>();
                    for (FileHandle fh : files) {
                        String path = base + stateLower + "/" + fh.name();
                        am.load(path, Texture.class);
                        am.finishLoadingAsset(path);
                        Texture tex = am.get(path, Texture.class);
                        frames.add(new TextureRegion(tex));
                    }
                    float fd = st == Estado.RUN ? RUN_SPEED : DEFAULT_SPEED;
                    map.put(st, new Animation<>(fd, frames));
                    Gdx.app.log(personajeName, st + " FOUND");
                }
            }
        }

        String jumpFile;
        String deathFile;
        String runFolder;
        String runPrefix;
        switch (personajeName.toLowerCase()) {
            case "roky":
                jumpFile = "Racoon_jump.png";
                deathFile = "Mapache_Skin.png";
                runFolder = "Mapache_Run";
                runPrefix = "Mapache";
                break;
            case "thumper":
                jumpFile = "Rabbit_jump.png";
                deathFile = "Conejo_Skin.png";
                runFolder = "Conejo_Run";
                runPrefix = "Conejo";
                break;
            case "orion":
            default:
                jumpFile = "Cat_jump.png";
                deathFile = "Gato_Skin.png";
                runFolder = "Gato_Run";
                runPrefix = "Gato";
                break;
        }
        String speedBase = "images/personajes/animaciones/Speedpaws_Char/";

        if (!map.containsKey(Estado.RUN)) {
            FileHandle dir = Gdx.files.internal(speedBase + runFolder);
            if (dir.exists()) {
                FileHandle[] files = dir.list("png");
                if (files != null && files.length > 0) {
                    Arrays.sort(files, Comparator.comparing(FileHandle::name));
                    Array<TextureRegion> frames = new Array<>();
                    for (FileHandle fh : files) {
                        String path = speedBase + runFolder + "/" + fh.name();
                        am.load(path, Texture.class);
                        am.finishLoadingAsset(path);
                        Texture tex = am.get(path, Texture.class);
                        frames.add(new TextureRegion(tex));
                    }
                    map.put(Estado.RUN, new Animation<>(RUN_SPEED, frames));
                    Gdx.app.log(personajeName, "RUN FALLBACK");
                }
            }
        }

        if (!map.containsKey(Estado.IDLE) && map.containsKey(Estado.RUN)) {
            TextureRegion first = map.get(Estado.RUN).getKeyFrames()[0];
            map.put(Estado.IDLE, new Animation<>(DEFAULT_SPEED, first));
            Gdx.app.log(personajeName, "IDLE FALLBACK");
        }

        if (!map.containsKey(Estado.JUMP)) {
            String path = speedBase + "Jump_pose/" + jumpFile;
            if (Gdx.files.internal(path).exists()) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                Texture tex = am.get(path, Texture.class);
                TextureRegion region = new TextureRegion(tex);
                map.put(Estado.JUMP, new Animation<>(DEFAULT_SPEED, region));
                Gdx.app.log(personajeName, "JUMP FALLBACK");
            }
        }

        if (!map.containsKey(Estado.FALL) && map.containsKey(Estado.JUMP)) {
            map.put(Estado.FALL, map.get(Estado.JUMP));
            Gdx.app.log(personajeName, "FALL FALLBACK");
        }

        if (!map.containsKey(Estado.ATTACK) && map.containsKey(Estado.IDLE)) {
            map.put(Estado.ATTACK, map.get(Estado.IDLE));
            Gdx.app.log(personajeName, "ATTACK FALLBACK");
        }

        if (!map.containsKey(Estado.HURT) && map.containsKey(Estado.IDLE)) {
            map.put(Estado.HURT, map.get(Estado.IDLE));
            Gdx.app.log(personajeName, "HURT FALLBACK");
        }

        if (!map.containsKey(Estado.DEAD)) {
            String path = speedBase + "Death/" + deathFile;
            if (Gdx.files.internal(path).exists()) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                Texture tex = am.get(path, Texture.class);
                TextureRegion region = new TextureRegion(tex);
                map.put(Estado.DEAD, new Animation<>(DEFAULT_SPEED, region));
                Gdx.app.log(personajeName, "DEAD FALLBACK");
            }
        }

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
                Gdx.app.log(personajeName, st + " FINAL_FALLBACK");
            }
        }

        return map;
    }
}

