package com.juegodiego.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.juegodiego.gfx.AnimationLoader;
import com.juegodiego.gfx.GdxDiagnostics;

/**
 * Thumper con doble salto.
 */
public class Thumper extends Personaje {
    private boolean doubleUsed;

    public Thumper(AssetManager manager, Vector2 spawn, GdxDiagnostics diag) {
        super("thumper", "Thumper", manager, spawn);
        anims.putAll(AnimationLoader.loadFor("thumper", manager, diag));
        speed = 240f;
        jumpForce = 520f;
        attackPower = 12;
        maxHP = hp = 110;
    }

    @Override
    protected void handleInput(float delta) {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (left && !right) moveLeft();
        else if (right && !left) moveRight();
        else stop();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (onGround) jump();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) attack();
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            if (!onGround && !doubleUsed && cooldownReady("doubleJump")) {
                doubleJump();
            }
        }
    }

    private void doubleJump() {
        velocity.y = jumpForce;
        doubleUsed = true;
        setCooldown("doubleJump", 1.5f);
        Gdx.app.log(nombre, "Double Jump!");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (onGround) {
            doubleUsed = false;
        }
    }
}
