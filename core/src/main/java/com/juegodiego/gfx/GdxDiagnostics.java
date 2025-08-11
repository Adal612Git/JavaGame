package com.juegodiego.gfx;

import com.badlogic.gdx.Gdx;
import com.juegodiego.personajes.Personaje.Estado;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Almacena y reporta información de animaciones cargadas.
 */
public class GdxDiagnostics {

    /** Datos de una animación individual. */
    public static class AnimInfo {
        public final String status;
        public final int frames;
        public final String sample;

        public AnimInfo(String status, int frames, String sample) {
            this.status = status;
            this.frames = frames;
            this.sample = sample;
        }
    }

    private final Map<String, EnumMap<Estado, AnimInfo>> data = new HashMap<>();

    /** Registra información de una animación para un personaje y estado. */
    public void record(String personaje, Estado estado, String status, int frames, String sample) {
        data.computeIfAbsent(personaje, k -> new EnumMap<>(Estado.class))
            .put(estado, new AnimInfo(status, frames, sample));
    }

    /** Imprime un reporte formateado de las animaciones de un personaje. */
    public void printReport(String personaje) {
        EnumMap<Estado, AnimInfo> map = data.get(personaje);
        Gdx.app.log("[LOADER]", "=== ANIM REPORT: " + personaje + " ===");
        Estado[] order = {Estado.IDLE, Estado.RUN, Estado.JUMP, Estado.FALL};
        if (map != null) {
            for (Estado st : order) {
                AnimInfo info = map.get(st);
                if (info != null) {
                    Gdx.app.log("[LOADER]", st + ": " + info.status + " frames=" + info.frames + " sample=" + info.sample);
                } else {
                    Gdx.app.log("[LOADER]", st + ": <no data>");
                }
            }
        }
        Gdx.app.log("[LOADER]", "=============================");
    }
}

