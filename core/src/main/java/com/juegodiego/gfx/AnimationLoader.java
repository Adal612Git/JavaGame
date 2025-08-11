package com.juegodiego.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
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
            Array<FileHandle> matches = new Array<>(FileHandle.class);

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
                Array<TextureRegion> frames = new Array<>(TextureRegion.class);
                Array<String> paths = new Array<>(String.class);
                for (FileHandle fh : matches) {
                    String path = dirPath + "/" + fh.name();
                    am.load(path, Texture.class);
                    am.finishLoadingAsset(path);
                    Texture tex = am.get(path, Texture.class);
                    tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    frames.add(new TextureRegion(tex));
                    paths.add(path);
                }
                float fd = st == Estado.RUN ? RUN_SPEED : DEFAULT_SPEED;
                TextureRegion[] arr = frames.toArray(TextureRegion.class);
                map.put(st, new Animation<>(fd, arr));
                loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class)).put(st, paths);
                String sample = paths.first();
                if (st == Estado.RUN) {
                    Gdx.app.log("[[LOADER]]", "RUN FOUND frames=" + matches.size + " source=std-path sample=" + sample);
                } else {
                    Gdx.app.log("[[" + personaje + "]]", "FOUND " + st + " frames=" + matches.size + " sample=" + sample);
                }
                diag.record(personaje, st, "FOUND", matches.size, sample);
            } else {
                if (st == Estado.RUN) {
                    Gdx.app.log("[[LOADER]]", "RUN MISSING @ " + dirPath + " (dir exists=" + dirExists + ")");
                    Gdx.app.log("[[LOADER]]", "RUN FOUND frames=0 source=std-path");
                } else {
                    Gdx.app.log("[[" + personaje + "]]", "MISSING " + st + " @ " + dirPath + " (dir exists=" + dirExists + ")");
                }
            }
        }

        // Fallback IDLE: RUN frame0 o Standing.png
        if (!map.containsKey(Estado.IDLE)) {
            Animation<TextureRegion> runAnim = map.get(Estado.RUN);
            if (runAnim != null && runAnim.getKeyFrames().length > 0) {
                TextureRegion first = runAnim.getKeyFrames()[0];
                map.put(Estado.IDLE, new Animation<>(DEFAULT_SPEED, first));
                EnumMap<Estado, Array<String>> pathMap =
                        loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class));
                Array<String> runPaths = pathMap.get(Estado.RUN);
                if (runPaths != null) {
                    pathMap.put(Estado.IDLE, runPaths);
                }
                Gdx.app.log("WARN", "Missing frames for IDLE in " + personaje + ". Fallback=RUN frame0");
                diag.record(personaje, Estado.IDLE, "FALLBACK", 1, "from RUN");
            } else {
                String standingPath = base + "Standing.png";
                FileHandle st = Gdx.files.internal(standingPath);
                if (st.exists()) {
                    am.load(standingPath, Texture.class);
                    am.finishLoadingAsset(standingPath);
                    Texture tex = am.get(standingPath, Texture.class);
                    tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    TextureRegion region = new TextureRegion(tex);
                    map.put(Estado.IDLE, new Animation<>(DEFAULT_SPEED, region));
                    Array<String> paths = new Array<>(String.class);
                    paths.add(standingPath);
                    loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class)).put(Estado.IDLE, paths);
                    Gdx.app.log("WARN", "Missing frames for IDLE in " + personaje + ". Fallback=Standing.png");
                } else {
                    Gdx.app.log("WARN", "Missing frames for IDLE in " + personaje + ". Fallback=NONE");
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
            Array<FileHandle> filtered = new Array<>(FileHandle.class);
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
                Array<TextureRegion> frames = new Array<>(TextureRegion.class);
                Array<String> paths = new Array<>(String.class);
                for (FileHandle fh : files) {
                    String path = fbDir + fh.name();
                    am.load(path, Texture.class);
                    am.finishLoadingAsset(path);
                    Texture tex = am.get(path, Texture.class);
                    tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    frames.add(new TextureRegion(tex));
                    paths.add(path);
                }
                TextureRegion[] arr = frames.toArray(TextureRegion.class);
                map.put(Estado.RUN, new Animation<>(RUN_SPEED, arr));
                loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class)).put(Estado.RUN, paths);
                Gdx.app.log("[[LOADER]]", "RUN FOUND frames=" + files.length + " source=speedpaws sample=" + paths.first());
                diag.record(personaje, Estado.RUN, "FALLBACK", files.length, paths.first());
            } else {
                Gdx.app.log("[[LOADER]]", "RUN MISSING @ " + fbDir + " (dir exists=" + dirExists + ")");
                Gdx.app.log("[[LOADER]]", "RUN FOUND frames=0 source=speedpaws");
            }
        }

        // Fallback RUN desde IDLE
        if (!map.containsKey(Estado.RUN)) {
            Animation<TextureRegion> idleAnim = map.get(Estado.IDLE);
            if (idleAnim != null && idleAnim.getKeyFrames().length > 0) {
                Animation<TextureRegion> runFromIdle =
                        new Animation<>(idleAnim.getFrameDuration(), idleAnim.getKeyFrames());
                map.put(Estado.RUN, runFromIdle);
                EnumMap<Estado, Array<String>> pathMap =
                        loadedPaths.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class));
                Array<String> idlePaths = pathMap.get(Estado.IDLE);
                if (idlePaths != null) {
                    pathMap.put(Estado.RUN, idlePaths);
                }
                Gdx.app.log("WARN", "Missing frames for RUN in " + personaje + ". Using IDLE as fallback.");
                diag.record(personaje, Estado.RUN, "IDLE_FALLBACK", idleAnim.getKeyFrames().length, "from IDLE");
            } else {
                Gdx.app.log("WARN", "Missing frames for RUN in " + personaje + ". No frames available.");
            }
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
                Texture tex = am.get(path, Texture.class);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                TextureRegion region = new TextureRegion(tex);
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
                Texture tex = am.get(path, Texture.class);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                TextureRegion region = new TextureRegion(tex);
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
                Texture tex = am.get(path, Texture.class);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                TextureRegion region = new TextureRegion(tex);
                map.put(Estado.DEAD, new Animation<>(DEFAULT_SPEED, region));
                diag.record(personaje, Estado.DEAD, "FALLBACK", 1, path);
                Gdx.app.log("[[" + personaje + "]]", "FALLBACK DEAD frames=1 sample=" + path);
            } else {
                Gdx.app.log("[[" + personaje + "]]", "MISSING DEAD @ " + path + " (dir exists=" + exists + ")");
            }
        }
        for (Estado st : Estado.values()) {
            if (!map.containsKey(st)) {
                Gdx.app.log("WARN", "Missing frames for " + st + " in " + personaje + ".");
            }
        }
        int idleCount = map.containsKey(Estado.IDLE) ? map.get(Estado.IDLE).getKeyFrames().length : 0;
        int runCount = map.containsKey(Estado.RUN) ? map.get(Estado.RUN).getKeyFrames().length : 0;
        int jumpCount = map.containsKey(Estado.JUMP) ? map.get(Estado.JUMP).getKeyFrames().length : 0;
        Gdx.app.log("INFO", "Anim loaded: " + personaje + " IDLE=" + idleCount + " RUN=" + runCount + " JUMP=" + jumpCount);
        return map;
    }
}

