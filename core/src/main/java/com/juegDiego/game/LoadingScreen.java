package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.juegodiego.JuegoDiegoGame;
import com.juegodiego.gfx.GdxDiagnostics;
import com.juegodiego.personajes.Orion;
import com.juegodiego.personajes.Personaje;
import com.juegodiego.personajes.Roky;
import com.juegodiego.personajes.Thumper;
import com.juegDiego.core.escenarios.Escenario;

/**
 * Pantalla de carga que prepara personaje y escenario.
 */
public class LoadingScreen implements Screen {
    private final Game game;
    private final String characterId;
    private SpriteBatch batch;
    private BitmapFont font;
    private float timer;
    private Personaje player;
    private Escenario escenario;

    public LoadingScreen(Game game, String characterId) {
        this.game = game;
        this.characterId = characterId;
    }

    @Override
    public void show() {
        ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.LOADING);
        batch = new SpriteBatch();
        font = new BitmapFont();
        timer = 0f;
        Gdx.app.log("Game", "Loading started (character=" + characterId + ", level=EscenarioPrueba)");

        AssetManager manager = new AssetManager();
        GdxDiagnostics diag = new GdxDiagnostics();
        if ("roky".equals(characterId)) {
            player = new Roky(manager, new Vector2(50, 200), diag);
        } else if ("thumper".equals(characterId)) {
            player = new Thumper(manager, new Vector2(50, 200), diag);
        } else {
            player = new Orion(manager, new Vector2(50, 200), diag);
        }
        Gdx.app.log("Game", "Character assets resolved: " + player.getNombre() + " basePath=images/personajes/" + characterId + "/");

        escenario = Escenario.crearEscenarioPrueba();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, "CARGANDO...", 20, 400);
        batch.end();

        timer += delta;
        if (timer >= 2f) {
            Gdx.app.log("Game", "Loading finished");
            ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.GAME);
            game.setScreen(new GameScreen(game, player, escenario));
        }
    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

