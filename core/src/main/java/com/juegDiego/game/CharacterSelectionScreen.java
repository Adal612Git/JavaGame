package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.juegodiego.JuegoDiegoGame;

/**
 * Menú textual para elegir un personaje.
 */
public class CharacterSelectionScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private final String[] ids = {"orion", "roky", "thumper"};
    private final String[] names = {"ORION", "ROKY", "THUMPER"};
    private int selected;

    public CharacterSelectionScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.MENU);
        batch = new SpriteBatch();
        font = new BitmapFont();
        Gdx.app.log("Game", "Menu shown");
    }

    private void highlight(int idx) {
        if (selected != idx) {
            selected = idx;
            Gdx.app.log("Game", "Character highlighted: " + names[selected]);
        }
    }

    private void select() {
        Gdx.app.log("Game", "Character selected: " + names[selected]);
        game.setScreen(new LoadingScreen(game, ids[selected]));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) highlight(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) highlight(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) highlight(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) select();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        batch.begin();
        font.draw(batch, "SELECCIONA PERSONAJE:", 20, 440);
        for (int i = 0; i < names.length; i++) {
            String prefix = (i == selected ? "> " : "  ");
            font.draw(batch, prefix + (i + 1) + ") " + names[i], 20, 400 - i * 30);
        }
        font.draw(batch, "Pulsa 1/2/3 para elegir. Enter para confirmar. ESC para salir.", 20, 260);
        batch.end();
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

