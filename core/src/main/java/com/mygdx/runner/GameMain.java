package com.mygdx.runner;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.mygdx.runner.screens.SelectScreen;

/**
 * Entry point for the runner game. Starts at the selection screen.
 */
public class GameMain extends Game {
    @Override
    public void create() {
        Gdx.app.log("INFO", "Boot Runner Game");
        setScreen(new SelectScreen(this));
    }
}
