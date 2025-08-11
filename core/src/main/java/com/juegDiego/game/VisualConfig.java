package com.juegDiego.game;

import com.badlogic.gdx.graphics.Color;

/**
 * Configuraci\u00f3n visual para overlays y efectos del jugador.
 */
public final class VisualConfig {
    /** Color y alpha del overlay NEON sutil. */
    public static final Color OVERLAY_COLOR = new Color(0.85f, 0.70f, 0.95f, 0.10f);

    /** Activar efecto de resaltado del jugador. */
    public static final boolean PLAYER_EFFECT_ACTIVE = true;

    /**
     * Par\u00e1metros del efecto del jugador (saturaci\u00f3n, contraste y brillo).
     */
    public static final float PLAYER_SATURATION = 1.35f;
    public static final float PLAYER_CONTRAST = 1.20f;
    public static final float PLAYER_BRIGHTNESS = 0.05f;

    private VisualConfig() {
    }
}
