package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.juegodiego.personajes.Personaje;

/**
 * Pantalla simple de carga antes de iniciar el nivel.
 */
public class LoadingScreen implements Screen {
    private final Game game;
    private final Personaje player;
    private SpriteBatch batch;
    private BitmapFont font;
    private float timer;

    public LoadingScreen(Game game, Personaje player) {
        this.game = game;
        this.player = player;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        timer = 0f;
        Gdx.app.log("Game", "Cargando escenario...");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, "Cargando...", 20, 400);
        batch.end();

        timer += delta;
        if (timer >= 2f) {
            Gdx.app.log("Game", "Carga completa");
            game.setScreen(new GameScreen(game, player));
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

