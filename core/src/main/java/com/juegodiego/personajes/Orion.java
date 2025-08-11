package com.juegodiego.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.juegodiego.gfx.AnimationLoader;
import com.juegodiego.gfx.GdxDiagnostics;

/**
 * Orion, personaje ágil con dash.
 */
public class Orion extends Personaje {
    private boolean dashing;
    private float dashTimer;

    public Orion(AssetManager manager, Vector2 spawn, GdxDiagnostics diag) {
        super("orion", "Orion", manager, spawn);
        anims.putAll(AnimationLoader.loadFor("orion", manager, diag));
        ensureRunAnim();
        speed = 260f;
        jumpForce = 520f;
        attackPower = 10;
        maxHP = hp = 100;
    }

    @Override
    protected void handleInput(float delta) {
        if (!dashing) {
            boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            if (left && !right) moveLeft();
            else if (right && !left) moveRight();
            else stop();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) jump();
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) attack();
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) dash();
    }

    private void dash() {
        if (!cooldownReady("dash")) {
            Gdx.app.log(nombre, "Dash CD: " + cooldowns.get("dash"));
            return;
        }
        setCooldown("dash", 2.5f);
        dashing = true;
        dashTimer = 0.2f;
        invulnerable = true;
        velocity.x = dir == Direccion.RIGHT ? speed * 3f : -speed * 3f;
        Gdx.app.log(nombre, "Dash!");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (dashing) {
            dashTimer -= delta;
            if (dashTimer <= 0) {
                dashing = false;
                invulnerable = false;
                stop();
            }
        }
    }
}
