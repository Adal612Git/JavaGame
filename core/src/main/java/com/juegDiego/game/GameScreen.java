package com.juegDiego.game;

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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.juegDiego.core.escenarios.Escenario;
import com.juegodiego.JuegoDiegoGame;
import com.juegodiego.personajes.Personaje;

public class GameScreen implements Screen {

    private final Game game;

    public static final float WORLD_W = 1280f;
    public static final float WORLD_H = 720f;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private OrthographicCamera uiCamera;
    private FitViewport uiViewport;

    private final Escenario escenario;
    private final Personaje player;
    private int hp = 100;
    private int score = 0;

    private Texture pixel;
    private BitmapFont font;

    public GameScreen(Game game, Personaje player, Escenario escenario) {
        this.game = game;
        this.player = player;
        this.escenario = escenario;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();
        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);

        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(WORLD_W, WORLD_H, uiCamera);
        uiViewport.apply();
        uiCamera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pixel.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();

        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            applyDamage("sim");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            addScore(10, "Pickup=item");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            addScore(20, "Kill/Objective");
        }

        player.update(delta);
        escenario.actualizar(delta);

        if (hp <= 0) {
            Gdx.app.log("Game", "Player died (hp=0, score=" + score + ")");
            ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.MENU);
            game.setScreen(new CharacterSelectionScreen(game));
            return;
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        escenario.dibujar(batch);
        player.render(batch);
        batch.end();

        drawHUD();
    }

    private void applyDamage(String source) {
        int old = hp;
        hp -= 10;
        if (hp < 0) hp = 0;
        if (hp != old) {
            Gdx.app.debug("Game", "HP changed: " + old + " -> " + hp);
        }
        Gdx.app.log("Game", "Damage source=" + source + " hp=" + hp);
    }

    private void addScore(int amount, String logPrefix) {
        int old = score;
        score += amount;
        if (score < 0) score = 0;
        if (score != old) {
            Gdx.app.debug("Game", "Score changed: " + old + " -> " + score);
        }
        Gdx.app.log("Game", logPrefix + " score=" + score);
    }

    private void drawHUD() {
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.draw(batch, "HP: " + hp, 20, WORLD_H - 20);
        float barW = 200f;
        float barH = 20f;
        batch.setColor(Color.DARK_GRAY);
        batch.draw(pixel, 20, WORLD_H - 40, barW, barH);
        batch.setColor(Color.RED);
        batch.draw(pixel, 20, WORLD_H - 40, barW * (hp / 100f), barH);
        batch.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score, 20, WORLD_H - 60);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        if (uiViewport != null) {
            uiViewport.update(width, height, true);
        }
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
        pixel.dispose();
        font.dispose();
    }
}

