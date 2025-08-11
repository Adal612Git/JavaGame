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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.juegDiego.core.escenarios.Escenario;
import com.juegDiego.core.escenarios.Plataforma;
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
    private float hp = 100f;
    private int score = 0;

    private Texture pixel;
    private BitmapFont font;

    private boolean paused;
    private Rectangle resumeRect;
    private Rectangle terminateRect;
    private Rectangle quitRect;

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

        float boxW = 220f;
        float boxH = 40f;
        resumeRect = new Rectangle(WORLD_W / 2f - boxW / 2f, WORLD_H / 2f + 40f, boxW, boxH);
        terminateRect = new Rectangle(WORLD_W / 2f - boxW / 2f, WORLD_H / 2f - boxH / 2f, boxW, boxH);
        quitRect = new Rectangle(WORLD_W / 2f - boxW / 2f, WORLD_H / 2f - 40f - boxH, boxW, boxH);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !paused) {
            paused = true;
            Gdx.app.log("INFO", "Pause opened");
        }

        if (!paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                applyDamage(10f, "Damage source=sim");
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                addScore(10, "Pickup=item");
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                addScore(20, "Kill/Objective");
            }

            player.update(delta);
            escenario.actualizar(delta);

            boolean onPlat = false;
            Rectangle b = player.getBounds();
            for (Plataforma p : escenario.getPlataformas()) {
                Rectangle pb = p.getBounds();
                if (b.overlaps(pb) && player.getVelocity().y <= 0 && b.y >= pb.y + pb.height - 10f) {
                    player.land(pb.y + pb.height);
                    onPlat = true;
                    break;
                }
            }
            if (!onPlat && player.isOnGround()) {
                player.leaveGround();
            }

            if (!player.isOnGround() && !onPlat) {
                float old = hp;
                hp -= 10f * delta;
                if (hp < 0f) hp = 0f;
                if ((int)old != (int)hp) {
                    Gdx.app.debug("Game", "HP changed: " + (int)old + " -> " + (int)hp);
                }
                Gdx.app.log("INFO", "Off-platform damage tick: hp=" + (int)hp);
            }

            if (player.getPosition().y < -50f) {
                float old = hp;
                hp -= 50f * delta;
                if (hp < 0f) hp = 0f;
                if ((int)old != (int)hp) {
                    Gdx.app.debug("Game", "HP changed: " + (int)old + " -> " + (int)hp);
                }
                Gdx.app.log("INFO", "Out-of-world damage tick: hp=" + (int)hp);
            }

            if (hp <= 0f) {
                Gdx.app.log("INFO", "Player died (hp=0, score=" + score + ")");
                ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.MENU);
                game.setScreen(new CharacterSelectionScreen(game));
                return;
            }
        } else {
            if (handlePauseInput()) {
                return;
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        escenario.dibujar(batch);
        player.render(batch);
        batch.end();

        drawHUD();
        if (paused) {
            drawPauseOverlay();
        }
    }

    private boolean handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            paused = false;
            Gdx.app.log("INFO", "Pause action: RESUME");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            Gdx.app.log("INFO", "Pause action: END_RUN");
            Gdx.app.log("INFO", "Run ended by user (score=" + score + ")");
            ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.MENU);
            game.setScreen(new CharacterSelectionScreen(game));
            return true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.log("INFO", "Pause action: QUIT");
            Gdx.app.exit();
            return true;
        }

        if (Gdx.input.justTouched()) {
            Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiCamera.unproject(v);
            float mx = v.x, my = v.y;
            if (resumeRect.contains(mx, my)) {
                paused = false;
                Gdx.app.log("INFO", "Pause action: RESUME");
            } else if (terminateRect.contains(mx, my)) {
                Gdx.app.log("INFO", "Pause action: END_RUN");
                Gdx.app.log("INFO", "Run ended by user (score=" + score + ")");
                ((JuegoDiegoGame) game).setState(JuegoDiegoGame.State.MENU);
                game.setScreen(new CharacterSelectionScreen(game));
                return true;
            } else if (quitRect.contains(mx, my)) {
                Gdx.app.log("INFO", "Pause action: QUIT");
                Gdx.app.exit();
                return true;
            }
        }
        return false;
    }

    private void applyDamage(float amount, String log) {
        float old = hp;
        hp -= amount;
        if (hp < 0f) hp = 0f;
        if (hp > 100f) hp = 100f;
        if ((int)old != (int)hp) {
            Gdx.app.debug("Game", "HP changed: " + (int)old + " -> " + (int)hp);
        }
        Gdx.app.log("Game", log + " hp=" + (int)hp);
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
        font.draw(batch, "HP: " + (int)hp, 20, WORLD_H - 20);
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

    private void drawPauseOverlay() {
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(pixel, 0, 0, WORLD_W, WORLD_H);
        batch.setColor(Color.WHITE);
        font.draw(batch, "PAUSA", WORLD_W / 2f - 40f, WORLD_H / 2f + 100f);
        font.draw(batch, "Score actual: " + score, WORLD_W / 2f - 80f, WORLD_H / 2f + 70f);
        font.draw(batch, "[R] Reanudar", resumeRect.x + 10, resumeRect.y + 25);
        font.draw(batch, "[T] Terminar partida y volver a Selección", terminateRect.x + 10, terminateRect.y + 25);
        font.draw(batch, "[Q] Salir del juego", quitRect.x + 10, quitRect.y + 25);
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

