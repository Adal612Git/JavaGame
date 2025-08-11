package com.juegDiego.game;

import com.badlogic.gdx.Game;

public class JavaGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}

