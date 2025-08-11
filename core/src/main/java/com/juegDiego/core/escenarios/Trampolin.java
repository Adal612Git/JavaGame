package com.juegDiego.core.escenarios;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Trampolín que impulsa al jugador hacia arriba.
 */
public class Trampolin extends Plataforma {
    private final float impulsoY;

    public Trampolin(float x, float y, float w, float h, float impulsoY, Texture texture) {
        super(x, y, w, h, texture);
        this.impulsoY = impulsoY;
    }

    public float getImpulsoY() {
        return impulsoY;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }
}
