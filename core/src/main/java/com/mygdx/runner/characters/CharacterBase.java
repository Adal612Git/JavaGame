package com.mygdx.runner.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runner.world.Track;

/**
 * Basic character with physics and rendering as colored rectangle.
 */
public class CharacterBase {
    private final String name;
    private final Color color;
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    private final float width = 32f;
    private final float height = 48f;
    private boolean onGround = true;
    private float jumpCooldown = 0f;

    public CharacterBase(String name, Color color, float startX) {
        this.name = name;
        this.color = color;
        position.set(startX, 0);
    }

    public void update(float delta, Track track) {
        jumpCooldown -= delta;
        // gravity
        velocity.y -= 900f * delta;
        // move
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        // ground
        if (position.y <= track.getGroundY()) {
            position.y = track.getGroundY();
            velocity.y = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        // obstacles
        Rectangle bounds = getBounds();
        for (Rectangle r : track.getObstacles()) {
            if (bounds.overlaps(r)) {
                position.x = r.x - width;
                bounds.x = position.x;
                velocity.x = 0;
            }
        }
        // friction
        if (onGround) {
            velocity.x *= 0.98f;
        }
    }

    public void jump(float force) {
        if (jumpCooldown <= 0f && onGround) {
            velocity.y = force;
            onGround = false;
            jumpCooldown = 0.4f; // 400ms
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, width, height);
    }

    public void render(SpriteBatch batch, com.badlogic.gdx.graphics.Texture pixel) {
        batch.setColor(color);
        batch.draw(pixel, position.x, position.y, width, height);
        batch.setColor(Color.WHITE);
    }

    public String getName() { return name; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isOnGround() { return onGround; }
}
