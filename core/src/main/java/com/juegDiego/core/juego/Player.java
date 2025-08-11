package com.juegDiego.core.juego;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa un jugador simple controlable para las pruebas de escenario.
 */
public class Player {
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private float speed = 200f;
    private final Rectangle bounds = new Rectangle();
    private final Texture texture;
    private float boostTimer;
    private float baseSpeed = speed;

    public Player(float x, float y) {
        this.position.set(x, y);
        this.texture = new Texture("images/personajes/orion/placeholder.png");
        this.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        updateBounds();
    }

    public void update(float delta) {
        handleInput();
        position.mulAdd(velocity, delta);
        if (boostTimer > 0) {
            boostTimer -= delta;
            if (boostTimer <= 0) {
                speed = baseSpeed;
            }
        }
        if (position.y < 0) position.y = 0;
        updateBounds();
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocity.x = -speed;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocity.x = speed;
        } else {
            velocity.x = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            velocity.y = 400f;
        }
        velocity.y += -980f * Gdx.graphics.getDeltaTime();
    }

    public void draw(SpriteBatch batch) {
        Color old = batch.getColor();
        batch.setColor(0.25f, 0.10f, 0.35f, 0.6f);
        batch.draw(texture, position.x - 1f, position.y, 64, 64);
        batch.draw(texture, position.x + 1f, position.y, 64, 64);
        batch.draw(texture, position.x, position.y - 1f, 64, 64);
        batch.draw(texture, position.x, position.y + 1f, 64, 64);
        batch.setColor(old);
        batch.draw(texture, position.x, position.y, 64, 64);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void applyBoost(float factor, float duration) {
        baseSpeed = speed / factor; // ensure base preserved
        speed = baseSpeed * factor;
        boostTimer = duration;
    }

    private void updateBounds() {
        bounds.set(position.x, position.y, 64, 64);
    }
}
