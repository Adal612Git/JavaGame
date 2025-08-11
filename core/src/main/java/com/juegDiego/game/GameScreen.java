package com.juegDiego.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.juegDiego.core.escenarios.Escenario;
import com.juegDiego.core.escenarios.Trampolin;
import com.juegDiego.core.juego.Player;
import com.juegDiego.game.VisualConfig;

public class GameScreen implements Screen {

    private final Game game;

    public static final float WORLD_W = 1280f;
    public static final float WORLD_H = 720f;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private Escenario escenario;
    private Player player;

    private Texture overlayTexture;
    private ShaderProgram playerShader;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();
        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);
        escenario = Escenario.crearEscenarioPrueba();
        player = new Player(50, 200);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        overlayTexture = new Texture(pm);
        overlayTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();

        if (VisualConfig.PLAYER_EFFECT_ACTIVE) {
            ShaderProgram.pedantic = false;
            String vert = "attribute vec4 a_position;\n" +
                    "attribute vec4 a_color;\n" +
                    "attribute vec2 a_texCoord0;\n" +
                    "uniform mat4 u_projTrans;\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "void main(){\n" +
                    "   v_color = a_color;\n" +
                    "   v_texCoords = a_texCoord0;\n" +
                    "   gl_Position =  u_projTrans * a_position;\n" +
                    "}";
            String frag = "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform float u_saturation;\n" +
                    "uniform float u_contrast;\n" +
                    "uniform float u_brightness;\n" +
                    "void main(){\n" +
                    "   vec4 color = texture2D(u_texture, v_texCoords) * v_color;\n" +
                    "   float grey = dot(color.rgb, vec3(0.2126,0.7152,0.0722));\n" +
                    "   color.rgb = mix(vec3(grey), color.rgb, u_saturation);\n" +
                    "   color.rgb = (color.rgb - 0.5) * u_contrast + 0.5;\n" +
                    "   color.rgb += u_brightness;\n" +
                    "   gl_FragColor = vec4(color.rgb, color.a);\n" +
                    "}";
            playerShader = new ShaderProgram(vert, frag);
            if (!playerShader.isCompiled()) {
                Gdx.app.error("Visual", "Player shader error: " + playerShader.getLog());
                playerShader = null;
            }
        }

        Gdx.app.log("Visual", "overlay=" + VisualConfig.OVERLAY_COLOR + " playerFX=" + (playerShader != null));
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

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        escenario.dibujar(batch);
        batch.setColor(VisualConfig.OVERLAY_COLOR);
        batch.draw(overlayTexture, 0, 0, WORLD_W, WORLD_H);
        batch.setColor(Color.WHITE);
        if (playerShader != null) {
            batch.setShader(playerShader);
            playerShader.setUniformf("u_saturation", VisualConfig.PLAYER_SATURATION);
            playerShader.setUniformf("u_contrast", VisualConfig.PLAYER_CONTRAST);
            playerShader.setUniformf("u_brightness", VisualConfig.PLAYER_BRIGHTNESS);
        }
        player.draw(batch);
        batch.setShader(null);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
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
        overlayTexture.dispose();
        if (playerShader != null) {
            playerShader.dispose();
        }
    }
}

