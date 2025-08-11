package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.juegodiego.gfx.GdxDiagnostics;
import com.juegodiego.personajes.Orion;
import com.juegodiego.personajes.Personaje;
import com.juegodiego.personajes.Roky;
import com.juegodiego.personajes.Thumper;

/**
 * Pantalla para seleccionar un personaje entre tres opciones.
 */
public class CharacterSelectionScreen implements Screen {
    private final Game game;
    private AssetManager manager;
    private GdxDiagnostics diag;
    private SpriteBatch batch;
    private BitmapFont font;
    private Personaje orion, roky, thumper;

    public CharacterSelectionScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        manager = new AssetManager();
        diag = new GdxDiagnostics();
        batch = new SpriteBatch();
        font = new BitmapFont();
        orion = new Orion(manager, new Vector2(200, 0), diag);
        roky = new Roky(manager, new Vector2(600, 0), diag);
        thumper = new Thumper(manager, new Vector2(1000, 0), diag);
    }

    private void select(Personaje p) {
        Gdx.app.log("Game", "Personaje seleccionado: " + p.getNombre());
        game.setScreen(new LoadingScreen(game, p));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        orion.update(delta);
        roky.update(delta);
        thumper.update(delta);

        batch.begin();
        font.draw(batch, "Selecciona un personaje: 1-Orion 2-Roky 3-Thumper", 20, 700);
        orion.render(batch);
        roky.render(batch);
        thumper.render(batch);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            select(orion);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            select(roky);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            select(thumper);
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
        // no se dispone el manager ni los personajes para reutilizarlos
    }
}

