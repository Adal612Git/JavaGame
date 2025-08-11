package com.juegodiego;

import com.badlogic.gdx.Game;
import com.juegodiego.screens.DemoScreen;

/**
 * Juego principal.
 */
public class JuegoDiegoGame extends Game {
    @Override
    public void create() {
        setScreen(new DemoScreen(this));
    }
}
