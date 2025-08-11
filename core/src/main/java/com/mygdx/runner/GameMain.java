package com.mygdx.runner;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.runner.screens.SelectScreen;

/**
 * Entry point for the runner game. Starts at the selection screen.
 */
public class GameMain extends Game {
    private final AssetManager assetManager = new AssetManager();

    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void create() {
        Gdx.app.log("INFO", "Boot Runner Game");
        loadAssets();
        assetManager.finishLoading();
        setScreen(new SelectScreen(this));
    }

    private void loadAssets() {
        // selection UI background (minimal boot)
        assetManager.load("assets/images/ui/seleccion_personajes.png", Texture.class);
        // TODO: load HUD fonts when available
    }

    @Override
    public void setScreen(Screen next) {
        Screen current = getScreen();
        if (current != null) {
            current.hide();
            current.dispose();
        }
        this.screen = next;
        if (next != null) next.show();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        assetManager.dispose();
    }
}
