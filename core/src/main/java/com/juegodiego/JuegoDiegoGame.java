package com.juegodiego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.juegDiego.game.CharacterSelectionScreen;

/**
 * Juego principal.
 */
public class JuegoDiegoGame extends Game {
    public enum State { MENU, LOADING, GAME }
    private State state;

    public void setState(State s) {
        state = s;
        Gdx.app.log("Game", "State -> " + s);
    }

    @Override
    public void create() {
        Gdx.app.log("INFO", "Boot OK (LWJGL3). user.dir=" + System.getProperty("user.dir") +
                " assetsRoot=" + Gdx.files.internal("").path());
        setState(State.MENU);
        setScreen(new CharacterSelectionScreen(this));
    }
}
