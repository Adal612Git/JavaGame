package com.juegodiego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.juegodiego.Const;
import com.juegodiego.JuegoDiegoGame;
import com.juegodiego.personajes.Orion;
import com.juegodiego.personajes.Personaje;
import com.juegodiego.personajes.Roky;
import com.juegodiego.personajes.Thumper;

/**
 * Pantalla de demostración.
 */
public class DemoScreen implements Screen {
    private final JuegoDiegoGame game;
    private final AssetManager manager;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ShapeRenderer shape;
    private Personaje personaje;

    public DemoScreen(JuegoDiegoGame game) {
        this.game = game;
        this.manager = new AssetManager();
        this.batch = new SpriteBatch();
        this.shape = new ShapeRenderer();
        this.camera = new OrthographicCamera(Const.VIEWPORT_WIDTH, Const.VIEWPORT_HEIGHT);
        this.camera.position.set(Const.VIEWPORT_WIDTH / 2f, Const.VIEWPORT_HEIGHT / 2f, 0);
        this.camera.update();
        spawnOrion(new Vector2(Const.VIEWPORT_WIDTH / 2f, 0));
    }

    private void spawnOrion(Vector2 pos) {
        disposePersonaje();
        personaje = new Orion(manager, pos);
    }

    private void spawnRoky(Vector2 pos) {
        disposePersonaje();
        personaje = new Roky(manager, pos);
    }

    private void spawnThumper(Vector2 pos) {
        disposePersonaje();
        personaje = new Thumper(manager, pos);
    }

    private void disposePersonaje() {
        if (personaje != null) {
            personaje.dispose();
        }
    }

    private void handleSpawnInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            spawnOrion(new Vector2(personaje != null ? personaje.getPosition().x : Const.VIEWPORT_WIDTH / 2f, 0));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            spawnRoky(new Vector2(personaje != null ? personaje.getPosition().x : Const.VIEWPORT_WIDTH / 2f, 0));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            spawnThumper(new Vector2(personaje != null ? personaje.getPosition().x : Const.VIEWPORT_WIDTH / 2f, 0));
        }
    }

    @Override
    public void render(float delta) {
        handleSpawnInput();
        if (personaje != null) {
            personaje.update(delta);
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (personaje != null) {
            personaje.render(batch);
        }
        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.2f, 0.6f, 0.2f, 1f);
        shape.rect(0, 0, Const.VIEWPORT_WIDTH, 5);
        shape.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        disposePersonaje();
        batch.dispose();
        manager.dispose();
        shape.dispose();
    }
}
