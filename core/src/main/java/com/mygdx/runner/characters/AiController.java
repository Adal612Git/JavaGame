package com.mygdx.runner.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.runner.world.Track;

/**
 * Simple AI to keep a target speed and jump over obstacles.
 */
public class AiController {
    private final CharacterBase character;
    private final Track track;
    private float targetSpeed;
    private float noiseTimer;
    private final float minSpeed;
    private final float maxSpeed;

    public AiController(CharacterBase c, Track t, float min, float max) {
        this.character = c;
        this.track = t;
        this.minSpeed = min;
        this.maxSpeed = max;
        this.targetSpeed = MathUtils.random(min, max);
        Gdx.app.log("INFO", "NPC[target=" + (int)targetSpeed + "]");
    }

    public void update(float delta) {
        noiseTimer -= delta;
        if (noiseTimer <= 0f) {
            targetSpeed += MathUtils.random(-10f, 10f);
            targetSpeed = MathUtils.clamp(targetSpeed, minSpeed, maxSpeed);
            noiseTimer = MathUtils.random(1f, 2f);
        }
        float accel = 500f;
        if (character.velocity.x < targetSpeed) character.velocity.x += accel * delta;
        if (character.velocity.x > targetSpeed) character.velocity.x -= accel * delta;

        // obstacle detection
        for (Rectangle r : track.getObstacles()) {
            float dx = r.x - (character.position.x + character.getWidth());
            if (dx >= 0 && dx <= 120 && character.isOnGround()) {
                character.jump(350f);
                break;
            }
        }
    }
}
