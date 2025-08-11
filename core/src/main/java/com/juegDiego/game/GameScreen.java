package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.juegDiego.core.escenarios.Escenario;
import com.juegDiego.core.escenarios.Trampolin;
import com.juegDiego.core.juego.Player;

public class GameScreen implements Screen {

    private final Game game;

    private SpriteBatch batch;

    private Escenario escenario;
    private Player player;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        escenario = Escenario.crearEscenarioPrueba();
        player = new Player(50, 200);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update(delta);
        escenario.actualizar(delta);

        for (Trampolin t : escenario.getTrampolines()) {
            if (escenario.intersectaPlayer(t, player)) {
                player.getVelocity().y = t.getImpulsoY();
                escenario.rampaVelocidad(player, 1.5f, 2f);
            }
        }

        batch.begin();
        escenario.dibujar(batch);
        player.draw(batch);
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
    }
}

