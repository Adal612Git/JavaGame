package com.juegodiego;

import com.badlogic.gdx.Gdx;

/**
 * Gestiona el sistema de puntuación y los artefactos temporales.
 *
 * <p>Este administrador sigue las reglas básicas descritas en el
 * documento de diseño: los artefactos TURBO y TRUENO tienen
 * duraciones limitadas, TURBO otorga puntos por segundo y TRUENO
 * duplica toda puntuación obtenida mientras esté activo. ESCUDO
 * y MOCHILA solo controlan temporizadores y efectos de otras
 * bonificaciones. PISTOLA maneja un contador de munición.</p>
 */
public class ScoreManager {

    private int score;
    private int boxesBroken;
    private int artifactsCollected;

    private float turboTime;
    private float turboTickAccum;
    private float truenoTime;
    private float escudoTime;
    private float mochilaTime;
    private int ammo;

    /**
     * Actualiza los temporizadores de los artefactos.
     *
     * @param delta tiempo transcurrido en segundos
     */
    public void update(float delta) {
        if (turboTime > 0f) {
            turboTime -= delta;
            turboTickAccum += delta;
            while (turboTickAccum >= 1f) {
                turboTickAccum -= 1f;
                addScore(2);
            }
            if (turboTime <= 0f) {
                turboTime = 0f;
                logInfo("Buff expired: TURBO");
            }
        }
        if (truenoTime > 0f) {
            truenoTime -= delta;
            if (truenoTime <= 0f) {
                truenoTime = 0f;
                logInfo("Buff expired: TRUENO");
            }
        }
        if (escudoTime > 0f) {
            escudoTime -= delta;
            if (escudoTime <= 0f) {
                escudoTime = 0f;
                logInfo("Buff expired: ESCUDO");
            }
        }
        if (mochilaTime > 0f) {
            mochilaTime -= delta;
            if (mochilaTime <= 0f) {
                mochilaTime = 0f;
                logInfo("Buff expired: MOCHILA");
            }
        }
    }

    /**
     * Registra la recolección de un artefacto TURBO.
     */
    public void pickupTurbo() {
        artifactsCollected++;
        float dur = 10f * durationMultiplier();
        turboTime = dur;
        logInfo("Pickup TURBO (+10). Boost active " + (int) dur + "s.");
        addScore(10);
    }

    /**
     * Registra la recolección de un artefacto TRUENO.
     */
    public void pickupTrueno() {
        artifactsCollected++;
        float dur = 8f * durationMultiplier();
        truenoTime = dur;
        logInfo("Pickup TRUENO (+15). Score x2 for " + (int) dur + "s.");
        addScore(15);
    }

    /**
     * Registra la recolección de un artefacto ESCUDO.
     */
    public void pickupEscudo() {
        artifactsCollected++;
        float dur = 6f * durationMultiplier();
        escudoTime = dur;
        logInfo("Pickup ESCUDO (+10). Hazard damage negated " + (int) dur + "s.");
        addScore(10);
    }

    /**
     * Registra la recolección de una PISTOLA.
     */
    public void pickupPistola() {
        artifactsCollected++;
        ammo = 10;
        logInfo("Pickup PISTOLA (+5). Ammo=10");
        addScore(5);
    }

    /**
     * Registra la recolección de una MOCHILA.
     */
    public void pickupMochila() {
        artifactsCollected++;
        mochilaTime = 30f;
        logInfo("Pickup MOCHILA (+10). Buff durations +50% for 30s.");
        addScore(10);
    }

    /**
     * Rompe una caja otorgando puntos y contabilizando la acción.
     */
    public void breakBox() {
        boxesBroken++;
        addScore(20);
    }

    /**
     * Dispara si hay munición disponible.
     */
    public void shoot() {
        if (ammo > 0) {
            ammo--;
            logDebug("Shoot. Ammo=" + ammo);
        }
    }

    /**
     * Añade puntuación aplicando el multiplicador de TRUENO si está activo.
     */
    private void addScore(int base) {
        int mult = (truenoTime > 0f) ? 2 : 1;
        int delta = base * mult;
        score += delta;
        logDebug("Score +" + base + " *mult=" + mult + " => " + delta + " | total=" + score);
    }

    private float durationMultiplier() {
        return mochilaTime > 0f ? 1.5f : 1f;
    }

    private static void logInfo(String msg) {
        if (Gdx.app != null) Gdx.app.log("INFO", msg);
    }

    private static void logDebug(String msg) {
        if (Gdx.app != null) Gdx.app.log("DEBUG", msg);
    }

    // Getters
    public int getScore() { return score; }
    public int getBoxesBroken() { return boxesBroken; }
    public int getArtifactsCollected() { return artifactsCollected; }
    public int getAmmo() { return ammo; }
    public boolean isTurboActive() { return turboTime > 0f; }
    public boolean isTruenoActive() { return truenoTime > 0f; }
    public boolean isEscudoActive() { return escudoTime > 0f; }
    public boolean isMochilaActive() { return mochilaTime > 0f; }
}

