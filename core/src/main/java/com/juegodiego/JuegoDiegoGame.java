package com.juegodiego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.juegDiego.game.GameScreen;

/**
 * Juego principal.
 */
public class JuegoDiegoGame extends Game {
    @Override
    public void create() {
        Gdx.app.log("[[ASSETS]]", "user.dir=" + System.getProperty("user.dir"));
        Gdx.app.log("[[ASSETS]]", "exists dir escenario=" + Gdx.files.internal("images/escenarios/ecenario_Ralph").exists());
        Gdx.app.log("[[ASSETS]]", "exists fondo=" + Gdx.files.internal("images/escenarios/ecenario_Ralph/ecenario_Ralph-01.png").exists());
        setScreen(new GameScreen(this));
    }
}
