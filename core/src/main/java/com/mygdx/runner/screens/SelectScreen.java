package com.mygdx.runner.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.runner.GameMain;

/**
 * Simple text based character selection screen.
 */
public class SelectScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture pixel;
    private Texture uiBg;
    private TextureRegion preview;
    private OrthographicCamera camera;
    private Viewport viewport;
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
        camera = new OrthographicCamera();
        viewport = new FitViewport(640,360,camera);
        viewport.apply();
        camera.position.set(320,180,0);
        camera.update();
        selected = 0;
        loadUiBg();
        updatePreview();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) { selected = (selected + 1) % ids.length; updatePreview(); }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) { selected = (selected - 1 + ids.length) % ids.length; updatePreview(); }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Gdx.app.log("INFO", "Character selected: " + ids[selected]);
            game.setScreen(new RaceScreen((GameMain)game, ids[selected]));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (uiBg != null) {
            batch.draw(uiBg, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }
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
        if (preview != null) {
            batch.draw(preview, 400, 120, 128, 128);
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "UP/DOWN select, ENTER confirm", 50, 50);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width,height,true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        pixel.dispose();
        // textures managed by AssetManager
    }

    private void loadUiBg() {
        GameMain gm = (GameMain) game;
        uiBg = gm.getAssetManager().get("assets/images/ui/seleccion_personajes.png", Texture.class);
        uiBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Gdx.app.log("INFO", "SelectScreen: fondo UI cargado OK");
    }

    private void updatePreview() {
        String id = ids[selected];
        GameMain gm = (GameMain) game;
        Texture tex = gm.getAssetManager().get("assets/images/personajes/" + id + "/placeholder.png", Texture.class);
        preview = new TextureRegion(tex);
        Gdx.app.log("INFO", "SelectScreen preview: " + id);
    }
}
