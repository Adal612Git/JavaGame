package com.juegDiego.core.escenarios;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.juegDiego.core.juego.Artefacto;

import java.util.Random;

/**
 * Caja que entrega un artefacto aleatorio.
 */
public class CajaArmas implements ElementoEscenario {
    private final Vector2 pos = new Vector2();
    private final Rectangle bounds = new Rectangle();
    private final Texture texture;

    public CajaArmas(float x, float y, float w, float h, Texture texture) {
        pos.set(x, y);
        bounds.set(x, y, w, h);
        this.texture = texture;
    }

    public Artefacto abrir(Random rng) {
        return Escenario.armaAleatoria(rng);
    }

    @Override
    public Vector2 getPos() {
        return pos;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    @Override
    public void update(float delta) {
        // animations or similar could be handled here
    }

    @Override
    public Texture getTexture() {
        return texture;
    }
}
