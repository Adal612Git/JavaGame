package com.juegDiego.core.escenarios;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Plataforma simple donde el jugador puede pararse.
 */
public class Plataforma implements ElementoEscenario {
    protected final Vector2 pos = new Vector2();
    protected final Rectangle bounds = new Rectangle();
    protected final Texture texture;

    public Plataforma(float x, float y, float w, float h, Texture texture) {
        this.pos.set(x, y);
        this.bounds.set(x, y, w, h);
        this.texture = texture;
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
        // no-op
    }
}
