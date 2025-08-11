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
import java.util.HashMap;
import java.util.Map;

/**
 * Carga animaciones para los personajes con diagnósticos detallados.
 */
public class AnimationLoader {
    private static final float RUN_SPEED = 0.06f;
    private static final float DEFAULT_SPEED = 0.1f;

    private static final Map<String, EnumMap<Estado, Array<String>>> loadedPaths = new HashMap<>();

    private AnimationLoader() {}

    /** Devuelve las rutas de los frames cargados para un personaje y estado. */
    public static Array<String> getFramePaths(String personaje, Estado st) {
        EnumMap<Estado, Array<String>> map = loadedPaths.get(personaje);
        return map != null ? map.get(st) : null;
    }

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
            boolean dirExists = dir.exists() && dir.isDirectory();
            FileHandle[] files = dirExists ? dir.list() : new FileHandle[0];
            int dirCount = files.length;
            Array<FileHandle> matches = new Array<>();

            if (st == Estado.RUN) {
                String[] pats = {"run_\\d+\\.png", "Run_\\d+\\.png", "Gato\\d+\\.png", "Mapache\\d+\\.png", "Conejo\\d+\\.png", ".*\\.png"};
                for (FileHandle fh : files) {
                    if (!"png".equals(fh.extension())) continue;
                    for (String p : pats) {
                        if (fh.name().matches(p)) {
                            matches.add(fh);
                            break;
                        }
                    }
                }
                matches.sort(Comparator.comparing(FileHandle::name));
                String first = matches.size > 0 ? matches.first().name() : "none";
                Gdx.app.log("[[LOADER]]", "RUN CHECK dir=" + dirPath + " patterns=" + Arrays.toString(pats) +
                        " listed=" + dirCount + " matched=" + matches.size + " first=" + first);
            } else if (st == Estado.IDLE) {
                FileHandle idle = dir.child("idle.png");
                if (idle.exists()) {
                    matches.add(idle);
                }
                Gdx.app.log("[[" + personaje + "]]", "CHECK dir=" + dirPath + " pattern=idle.png count=" + (idle.exists() ? 1 : 0) +
                        " exists=" + dirExists + " list=" + dirCount);
                if (matches.size == 0) {
                    for (FileHandle fh : files) {
                        if (fh.extension().equals("png") && fh.name().matches("idle_\\d+\\.png")) {
                            matches.add(fh);
                        }
                    }
                    Gdx.app.log("[[" + personaje + "]]", "CHECK dir=" + dirPath + " pattern=idle_####.png count=" + matches.size +
                            " exists=" + dirExists + " list=" + dirCount);
                }
            } else {
                for (FileHandle fh : files) {
                    if ("png".equals(fh.extension())) {
                        matches.add(fh);
                    }
                }
                Gdx.app.log("[[" + personaje + "]]", "CHECK dir=" + dirPath + " pattern=*.png count=" + matches.size +
                        " exists=" + dirExists + " list=" + dirCount);
            }

            if (matches.size > 0) {
                matches.sort(Comparator.comparing(FileHandle::name));
                Array<TextureRegion> frames = new Array<>();
                Array<String> paths = new Array<>();
                for (FileHandle fh : matches) {
                    String path = dirPath + "/" + fh.name();
                    am.load(path, Texture.class);
                    am.finishLoadingAsset(path);
                    frames.add(new TextureRegion(am.get(path, Texture.class)));
                    paths.add(path);
                }
                float fd = st == Estado.RUN ? RUN_SPEED : DEFAULT_SPEED;
                map.put(st, new Animation<>(fd, frames));
                loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class)).put(st, paths);
                String sample = paths.first();
                if (st == Estado.RUN) {
                    Gdx.app.log("[[LOADER]]", "RUN FOUND frames=" + matches.size + " sample=" + sample);
                } else {
                    Gdx.app.log("[[" + personaje + "]]", "FOUND " + st + " frames=" + matches.size + " sample=" + sample);
                }
                diag.record(personaje, st, "FOUND", matches.size, sample);
            } else {
                if (st == Estado.RUN) {
                    Gdx.app.log("[[LOADER]]", "RUN MISSING @ " + dirPath + " (dir exists=" + dirExists + ")");
                } else {
                    Gdx.app.log("[[" + personaje + "]]", "MISSING " + st + " @ " + dirPath + " (dir exists=" + dirExists + ")");
                }
            }
        }

        // Mapeo de animal para fallbacks
        String animal;
        switch (personaje.toLowerCase()) {
            case "roky":
                animal = "Mapache";
                break;
            case "thumper":
                animal = "Conejo";
                break;
            case "orion":
            default:
                animal = "Gato";
                break;
        }
        String speedBase = "images/personajes/animaciones/Speedpaws_Char/";

        // Fallback RUN
        if (!map.containsKey(Estado.RUN)) {
            String fbDir = speedBase + animal + "_Run/";
            FileHandle dir = Gdx.files.internal(fbDir);
            boolean dirExists = dir.exists() && dir.isDirectory();
            FileHandle[] raw = dirExists ? dir.list() : new FileHandle[0];
            int listCount = raw.length;
            Array<FileHandle> filtered = new Array<>();
            for (FileHandle fh : raw) {
                if ("png".equals(fh.extension())) {
                    filtered.add(fh);
                }
            }
            FileHandle[] files = filtered.toArray(FileHandle.class);
            Arrays.sort(files, Comparator.comparing(FileHandle::name));
            Gdx.app.log("[[LOADER]]", "RUN CHECK dir=" + fbDir + " patterns=[*.png] listed=" + listCount + " matched=" + files.length +
                    " first=" + (files.length > 0 ? files[0].name() : "none"));
            if (files.length > 0) {
                Array<TextureRegion> frames = new Array<>();
                Array<String> paths = new Array<>();
                for (FileHandle fh : files) {
                    String path = fbDir + fh.name();
                    am.load(path, Texture.class);
                    am.finishLoadingAsset(path);
                    frames.add(new TextureRegion(am.get(path, Texture.class)));
                    paths.add(path);
                }
                map.put(Estado.RUN, new Animation<>(RUN_SPEED, frames));
                loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class)).put(Estado.RUN, paths);
                String sample = paths.first();
                Gdx.app.log("[[LOADER]]", "RUN FALLBACK frames=" + files.length + " sample=" + sample);
                diag.record(personaje, Estado.RUN, "FALLBACK", files.length, sample);
            } else {
                Gdx.app.log("[[LOADER]]", "RUN MISSING @ " + fbDir + " (dir exists=" + dirExists + ")");
            }
        }

        // Fallback IDLE desde RUN
        if (!map.containsKey(Estado.IDLE) && map.containsKey(Estado.RUN)) {
            TextureRegion first = map.get(Estado.RUN).getKeyFrames()[0];
            map.put(Estado.IDLE, new Animation<>(DEFAULT_SPEED, first));
            diag.record(personaje, Estado.IDLE, "FALLBACK", 1, "from RUN");
            Gdx.app.log("[[" + personaje + "]]", "FALLBACK IDLE frames=1 sample=from RUN");
        }

        // Fallback JUMP y FALL
        String jumpFile;
        switch (animal) {
            case "Mapache":
                jumpFile = "Racoon_jump.png";
                break;
            case "Conejo":
                jumpFile = "Rabbit_jump.png";
                break;
            case "Gato":
            default:
                jumpFile = "Cat_jump.png";
                break;
        }

        if (!map.containsKey(Estado.JUMP)) {
            String path = speedBase + "Jump_pose/" + jumpFile;
            FileHandle fh = Gdx.files.internal(path);
            boolean exists = fh.exists();
            Gdx.app.log("[[" + personaje + "]]", "CHECK dir=" + path + " pattern=file count=" + (exists ? 1 : 0) +
                    " exists=" + exists + " list=" + (exists ? 1 : 0));
            if (exists) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                TextureRegion region = new TextureRegion(am.get(path, Texture.class));
                map.put(Estado.JUMP, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.JUMP, "FALLBACK", 1, path);
                Gdx.app.log("[[" + personaje + "]]", "FALLBACK JUMP frames=1 sample=" + path);
            } else {
                Gdx.app.log("[[" + personaje + "]]", "MISSING JUMP @ " + path + " (dir exists=" + exists + ")");
            }
        }

        if (!map.containsKey(Estado.FALL)) {
            String path = speedBase + "Jump_pose/" + jumpFile;
            FileHandle fh = Gdx.files.internal(path);
            boolean exists = fh.exists();
            Gdx.app.log("[[" + personaje + "]]", "CHECK dir=" + path + " pattern=file count=" + (exists ? 1 : 0) +
                    " exists=" + exists + " list=" + (exists ? 1 : 0));
            if (exists) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                TextureRegion region = new TextureRegion(am.get(path, Texture.class));
                map.put(Estado.FALL, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.FALL, "FALLBACK", 1, path);
                Gdx.app.log("[[" + personaje + "]]", "FALLBACK FALL frames=1 sample=" + path);
            } else {
                Gdx.app.log("[[" + personaje + "]]", "MISSING FALL @ " + path + " (dir exists=" + exists + ")");
            }
        }

        // Fallback ATTACK y HURT desde IDLE
        if (!map.containsKey(Estado.ATTACK) && map.containsKey(Estado.IDLE)) {
            map.put(Estado.ATTACK, map.get(Estado.IDLE));
            diag.record(personaje, Estado.ATTACK, "FALLBACK", 1, "from IDLE");
            Gdx.app.log("[[" + personaje + "]]", "FALLBACK ATTACK frames=1 sample=from IDLE");
        }
        if (!map.containsKey(Estado.HURT) && map.containsKey(Estado.IDLE)) {
            map.put(Estado.HURT, map.get(Estado.IDLE));
            diag.record(personaje, Estado.HURT, "FALLBACK", 1, "from IDLE");
            Gdx.app.log("[[" + personaje + "]]", "FALLBACK HURT frames=1 sample=from IDLE");
        }

        // Fallback DEAD
        String deathFile;
        switch (animal) {
            case "Mapache":
                deathFile = "Mapache_Skin.png";
                break;
            case "Conejo":
                deathFile = "Conejo_Skin.png";
                break;
            case "Gato":
            default:
                deathFile = "Gato_Skin.png";
                break;
        }
        if (!map.containsKey(Estado.DEAD)) {
            String path = speedBase + "Death/" + deathFile;
            FileHandle fh = Gdx.files.internal(path);
            boolean exists = fh.exists();
            Gdx.app.log("[[" + personaje + "]]", "CHECK dir=" + path + " pattern=file count=" + (exists ? 1 : 0) +
                    " exists=" + exists + " list=" + (exists ? 1 : 0));
            if (exists) {
                am.load(path, Texture.class);
                am.finishLoadingAsset(path);
                TextureRegion region = new TextureRegion(am.get(path, Texture.class));
                map.put(Estado.DEAD, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.DEAD, "FALLBACK", 1, path);
                Gdx.app.log("[[" + personaje + "]]", "FALLBACK DEAD frames=1 sample=" + path);
            } else {
                Gdx.app.log("[[" + personaje + "]]", "MISSING DEAD @ " + path + " (dir exists=" + exists + ")");
            }
        }

        // Final fallback
        for (Estado st : Estado.values()) {
            if (!map.containsKey(st)) {
                Color color;
                switch (st) {
                    case RUN:
                        color = Color.BLUE;
                        break;
                    case JUMP:
                        color = Color.YELLOW;
                        break;
                    case FALL:
                        color = Color.ORANGE;
                        break;
                    case ATTACK:
                        color = Color.RED;
                        break;
                    case HURT:
                        color = Color.PURPLE;
                        break;
                    case DEAD:
                        color = Color.GRAY;
                        break;
                    case IDLE:
                    default:
                        color = Color.GREEN;
                        break;
                }
                Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pm.setColor(color);
                pm.fill();
                Texture tex = new Texture(pm);
                pm.dispose();
                TextureRegion region = new TextureRegion(tex);
                map.put(st, new Animation<>(DEFAULT_SPEED, region));
                Array<String> paths = new Array<>();
                paths.add("solid-color");
                loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class)).put(st, paths);
                diag.record(personaje, st, "FINAL_FALLBACK", 1, "solid-color");
                if (st == Estado.RUN) {
                    Gdx.app.log("[[LOADER]]", "RUN FINAL_FALLBACK");
                } else {
                    Gdx.app.log("[[" + personaje + "]]", "FINAL_FALLBACK " + st);
                }
            }
        }

        return map;
    }
}

