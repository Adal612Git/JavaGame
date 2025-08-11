package com.juegDiego.core.escenarios;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Comportamiento básico que todos los elementos del escenario deben cumplir.
 */
public interface ElementoEscenario {
    Vector2 getPos();
    Rectangle getBounds();
    void draw(SpriteBatch batch);
    void update(float delta);
}
