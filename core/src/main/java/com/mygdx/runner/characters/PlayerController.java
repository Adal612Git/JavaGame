package com.mygdx.runner.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;

/**
 * Handles keyboard input for the player.
 */
public class PlayerController {
    private final CharacterBase character;
    private final float accel = 800f;
    private final float jump = 350f;
    private final float longJump = 500f;

    public PlayerController(CharacterBase c) {
        this.character = c;
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) character.velocity.x += accel * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) character.velocity.x -= accel * delta;
        character.velocity.x = MathUtils.clamp(character.velocity.x, -character.getMaxSpeedX(), character.getMaxSpeedX());
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) character.jump(jump);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) character.jump(longJump);
    }
}
