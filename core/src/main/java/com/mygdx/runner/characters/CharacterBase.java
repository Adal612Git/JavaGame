package com.mygdx.runner.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runner.world.Track;

import java.util.EnumMap;

/**
 * Basic character with physics and sprite animations.
 */
public class CharacterBase {
    private final String name;
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    private final float width = 32f;
    private final float height = 48f;
    private boolean onGround = true;
    private float jumpCooldown = 0f;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private final EnumMap<State, Animation<TextureRegion>> anims = new EnumMap<>(State.class);
    private final AssetManager assetManager;

    private final float maxSpeedX;
    private final float frictionX;

    public CharacterBase(String name, float startX, AssetManager am) {
        this(name, startX, am, 250f, 6f);
    }

    public CharacterBase(String name, float startX, AssetManager am, float maxSpeedX, float frictionX) {
        this.name = name;
        position.set(startX, 0);
        this.maxSpeedX = maxSpeedX;
        this.frictionX = frictionX;
        this.assetManager = am;
        Texture tex = am.get("assets/images/personajes/" + name + "/placeholder.png", Texture.class);
        TextureRegion region = new TextureRegion(tex);
        Animation<TextureRegion> anim = new Animation<>(0.1f, region);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        anims.put(State.IDLE, anim);
        anims.put(State.RUN, anim);
        anims.put(State.JUMP, anim);
        anims.put(State.FALL, anim);
        Gdx.app.log("INFO", "Character " + name + " loaded (placeholder)");
    }

    public void update(float delta, Track track) {
        jumpCooldown -= delta;
        stateTime += delta;
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
            float drag = frictionX * delta;
            velocity.x -= velocity.x * drag;
        }
        // clamp horizontal speed
        velocity.x = MathUtils.clamp(velocity.x, -maxSpeedX, maxSpeedX);
        if (velocity.x > 0) facingRight = true;
        else if (velocity.x < 0) facingRight = false;
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

    private State resolveState() {
        if (!onGround) return velocity.y >= 0 ? State.JUMP : State.FALL;
        return Math.abs(velocity.x) > 1f ? State.RUN : State.IDLE;
    }

    public void render(SpriteBatch batch) {
        State st = resolveState();
        Animation<TextureRegion> anim = anims.get(st);
        if (anim == null) anim = anims.get(State.IDLE);
        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        if (facingRight && frame.isFlipX()) frame.flip(true, false);
        if (!facingRight && !frame.isFlipX()) frame.flip(true, false);
        float drawW = width * 1.35f;
        float drawH = height * 1.35f;
        float drawX = position.x - (drawW - width) / 2f;
        float drawY = position.y;
        batch.draw(frame, drawX, drawY, drawW, drawH);
    }

    public void dispose() { }

    public String getName() { return name; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isOnGround() { return onGround; }
    public float getMaxSpeedX() { return maxSpeedX; }
}
