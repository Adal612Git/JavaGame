package com.juegodiego.screens;

/*
# Desde la raíz del repo:
# ./gradlew tasks | grep -i run
# ./gradlew lwjgl3:run    # o desktop:run

# Teclas de prueba:
# Movimiento: A/D o ←/→
# Salto: SPACE
# Ataque: J
# Habilidad: K
# Cambiar personaje: 1 (Orion), 2 (Roky), 3 (Thumper)
*/

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
import com.juegodiego.gfx.GdxDiagnostics;

/**
 * Pantalla de demostración.
 */
public class DemoScreen implements Screen {
    private final JuegoDiegoGame game;
    private final AssetManager manager;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ShapeRenderer shape;
    private final GdxDiagnostics diag;
    private Personaje personaje;
    private float logTimer;

    public DemoScreen(JuegoDiegoGame game) {
        this.game = game;
        this.manager = new AssetManager();
        this.batch = new SpriteBatch();
        this.shape = new ShapeRenderer();
        this.camera = new OrthographicCamera(Const.VIEWPORT_WIDTH, Const.VIEWPORT_HEIGHT);
        this.camera.position.set(Const.VIEWPORT_WIDTH / 2f, Const.VIEWPORT_HEIGHT / 2f, 0);
        this.camera.update();
        this.diag = new GdxDiagnostics();

        Gdx.app.log("[ASSETS]", "java.version=" + System.getProperty("java.version"));
        Gdx.app.log("[ASSETS]", "os.name=" + System.getProperty("os.name"));
        Gdx.app.log("[ASSETS]", "user.dir=" + System.getProperty("user.dir"));
        logExists("images/personajes");
        logExists("images/personajes/orion/run/Gato0001.png");
        logExists("images/personajes/animaciones/Speedpaws_Char/Gato_Run/Gato0001.png");
        logExists("images/personajes/animaciones/Speedpaws_Char/Jump_pose/Cat_jump.png");
        logExists("images/personajes/orion/idle/idle.png");

        spawnOrion(new Vector2(Const.VIEWPORT_WIDTH / 2f, 0));
    }

    private void spawnOrion(Vector2 pos) {
        disposePersonaje();
        personaje = new Orion(manager, pos, diag);
        diag.printReport("orion");
    }

    private void spawnRoky(Vector2 pos) {
        disposePersonaje();
        personaje = new Roky(manager, pos, diag);
        diag.printReport("roky");
    }

    private void spawnThumper(Vector2 pos) {
        disposePersonaje();
        personaje = new Thumper(manager, pos, diag);
        diag.printReport("thumper");
    }

    private void disposePersonaje() {
        if (personaje != null) {
            personaje.dispose();
        }
    }

    private void handleSpawnInput() {
        Vector2 pos = personaje != null ? new Vector2(personaje.getPosition())
                : new Vector2(Const.VIEWPORT_WIDTH / 2f, 0);
        Personaje.Direccion dir = personaje != null ? personaje.getDir() : Personaje.Direccion.RIGHT;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            Gdx.app.log("[INPUT]", "spawn orion");
            spawnOrion(pos);
            personaje.setDir(dir);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            Gdx.app.log("[INPUT]", "spawn roky");
            spawnRoky(pos);
            personaje.setDir(dir);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            Gdx.app.log("[INPUT]", "spawn thumper");
            spawnThumper(pos);
            personaje.setDir(dir);
        }
    }

    @Override
    public void render(float delta) {
        handleSpawnInput();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1) && personaje != null) {
            personaje.debugDrawHitbox = !personaje.debugDrawHitbox;
            Gdx.app.log("[INPUT]", "debugDrawHitbox=" + personaje.debugDrawHitbox);
        }
        if (personaje != null) {
            personaje.update(delta);
            camera.position.x = personaje.getPosition().x;
            camera.position.y = Const.VIEWPORT_HEIGHT / 2f;
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

        if (personaje != null && personaje.debugDrawHitbox) {
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(1f, 0f, 0f, 1f);
            personaje.renderDebug(shape);
            shape.end();
        }

        if (personaje != null) {
            logTimer += delta;
            if (logTimer >= 0.5f) {
                logTimer = 0f;
                Vector2 p = personaje.getPosition();
                Gdx.app.log("[[RENDER]]", String.format(
                        "state=%s draw=(%.1f,%.1f,%.1f,%.1f) frameWH=(%.0f,%.0f) cam=(%.1f,%.1f) vp=(%.1f,%.1f)",
                        personaje.getEstado(), p.x, p.y,
                        personaje.getLastDrawWidth(), personaje.getLastDrawHeight(),
                        personaje.getLastFrameWidth(), personaje.getLastFrameHeight(),
                        camera.position.x, camera.position.y,
                        camera.viewportWidth, camera.viewportHeight));
            }
        }
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

    private void logExists(String path) {
        boolean exists = Gdx.files.internal(path).exists();
        Gdx.app.log("[ASSETS]", path + " exists=" + exists);
    }
}
