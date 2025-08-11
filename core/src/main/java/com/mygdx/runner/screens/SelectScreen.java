package com.mygdx.runner.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.runner.GameMain;

/**
 * Simple text based character selection screen.
 */
public class SelectScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture pixel;
    private int selected;
    private final String[] ids = {"orion", "roky", "thumper"};
    private final String[] names = {"ORION", "ROKY", "THUMPER"};

    public SelectScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
        selected = 0;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selected = (selected + 1) % ids.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) selected = (selected - 1 + ids.length) % ids.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Gdx.app.log("INFO", "Character selected: " + ids[selected]);
            game.setScreen(new RaceScreen((GameMain)game, ids[selected]));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        batch.begin();
        for (int i=0;i<ids.length;i++) {
            String label = names[i];
            float x = 100;
            float y = 300 - i*30;
            if (i==selected) {
                font.setColor(Color.YELLOW);
                batch.draw(pixel, x-20, y-20, 200, 25);
            } else {
                font.setColor(Color.WHITE);
            }
            font.draw(batch, label, x, y);
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "UP/DOWN select, ENTER confirm", 50, 50);
        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        pixel.dispose();
    }
}
