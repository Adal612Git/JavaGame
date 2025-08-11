package com.juegodiego.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.juegodiego.gfx.AnimationLoader;

/**
 * Roky, tanque con guardia.
 */
public class Roky extends Personaje {
    private boolean guarding;
    private float guardTimer;

    public Roky(AssetManager manager, Vector2 spawn) {
        super("roky", "Roky", manager, spawn);
        anims.putAll(AnimationLoader.loadFor("roky", manager));
        speed = 190f;
        jumpForce = 480f;
        attackPower = 14;
        maxHP = hp = 140;
    }

    @Override
    protected void handleInput(float delta) {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (left && !right) moveLeft();
        else if (right && !left) moveRight();
        else stop();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) jump();
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) attack();
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) guard();
    }

    private void guard() {
        if (!cooldownReady("guard")) {
            Gdx.app.log(nombre, "Guard CD: " + cooldowns.get("guard"));
            return;
        }
        setCooldown("guard", 4f);
        guarding = true;
        guardTimer = 1f;
        Gdx.app.log(nombre, "Guard!");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (guarding) {
            guardTimer -= delta;
            if (guardTimer <= 0) {
                guarding = false;
            }
        }
    }

    @Override
    public void takeDamage(int dmg) {
        if (guarding) {
            dmg *= 0.3f;
        }
        super.takeDamage(dmg);
    }
}
