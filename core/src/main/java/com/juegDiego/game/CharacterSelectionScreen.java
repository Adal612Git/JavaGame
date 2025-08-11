package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import com.juegodiego.JuegoDiegoGame;

/**
 * Menú textual para elegir un personaje.
 */
public class CharacterSelectionScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture image;
    private Texture pixel;
    private Rectangle[] rects = new Rectangle[3];
    private String imageName;
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
        String path = "images/ui/seleccion_personajes.png";
        if (!Gdx.files.internal(path).exists()) {
            path = "images/ui/seleccion personajes.png";
            if (Gdx.files.internal(path).exists()) {
                Gdx.app.log("WARN", "UI image has space in filename; consider renaming to seleccion_personajes.png");
            }
        }
        image = new Texture(Gdx.files.internal(path));
        image.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        imageName = new java.io.File(path).getName();
        Gdx.app.log("INFO", "Selection UI image used: " + imageName);
        Gdx.app.log("INFO", "Menu shown (image=seleccion_personajes)");
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
    }

    private void highlight(int idx) {
        if (selected != idx) {
            selected = idx;
            Gdx.app.log("INFO", "Character highlighted: " + names[selected]);
        }
    }

    private void select() {
        Gdx.app.log("INFO", "Character selected: " + names[selected]);
        game.setScreen(new LoadingScreen(game, ids[selected]));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        float iw = image.getWidth();
        float ih = image.getHeight();
        float scale = Math.min(w / iw, h / ih);
        float drawW = iw * scale;
        float drawH = ih * scale;
        float x = (w - drawW) / 2f;
        float y = (h - drawH) / 2f;

        float zoneW = drawW / 3f;
        for (int i = 0; i < 3; i++) {
            rects[i] = new Rectangle(x + i * zoneW, y, zoneW, drawH);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) highlight(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) highlight(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) highlight(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) select();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
        if (Gdx.input.justTouched()) {
            int mx = Gdx.input.getX();
            int my = h - Gdx.input.getY();
            for (int i = 0; i < rects.length; i++) {
                if (rects[i].contains(mx, my)) {
                    highlight(i);
                    break;
                }
            }
        }

        batch.begin();
        batch.draw(image, x, y, drawW, drawH);
        Rectangle r = rects[selected];
        batch.setColor(Color.YELLOW);
        batch.draw(pixel, r.x, r.y, r.width, 3);
        batch.draw(pixel, r.x, r.y + r.height - 3, r.width, 3);
        batch.draw(pixel, r.x, r.y, 3, r.height);
        batch.draw(pixel, r.x + r.width - 3, r.y, 3, r.height);
        batch.setColor(Color.WHITE);
        font.draw(batch, "Selecciona personaje: [1] ORION   [2] ROKY   [3] THUMPER", 20, 60);
        font.draw(batch, "Pulsa 1/2/3 para elegir. ENTER para confirmar. ESC para salir.", 20, 30);
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
        image.dispose();
        pixel.dispose();
    }
}

