package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen implements Screen {

    private final Game game;

    private SpriteBatch batch;
    private Texture fondo;
    private Texture orion;
    private Texture roky;
    private Texture thumper;
    private Texture caja;
    private Texture escudo;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        fondo = new Texture("images/escenarios/ralph_bg.png");
        orion = new Texture("images/personajes/orion/placeholder.png");
        roky = new Texture("images/personajes/roky/placeholder.png");
        thumper = new Texture("images/personajes/thumper/placeholder.png");
        caja = new Texture("images/artefactos/caja.png");
        escudo = new Texture("images/artefactos/escudo.png");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(orion, 100, 100);
        batch.draw(roky, 300, 100);
        batch.draw(thumper, 500, 100);
        batch.draw(caja, 150, 220);
        batch.draw(escudo, 380, 220);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        fondo.dispose();
        orion.dispose();
        roky.dispose();
        thumper.dispose();
        caja.dispose();
        escudo.dispose();
    }
}

