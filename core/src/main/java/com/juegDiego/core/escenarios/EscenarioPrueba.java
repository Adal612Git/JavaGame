package com.juegDiego.core.escenarios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.juegDiego.game.GameScreen;
import java.io.FileNotFoundException;

/**
 * Implementación sencilla de un escenario para pruebas.
 */
public class EscenarioPrueba extends Escenario {
    private final Texture fondo;
    private final Texture plataformaTx;
    private final Texture trampolinTx;
    private final Texture obstaculoTx;
    private final Texture cajaTx;
    private final ShapeRenderer bgDebug = new ShapeRenderer();

    public EscenarioPrueba() {
        fondo = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-01.png");
        if (fondo != null) fondo.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        plataformaTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-02.png");
        if (plataformaTx != null) plataformaTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        trampolinTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-03.png");
        if (trampolinTx != null) trampolinTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        obstaculoTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-04.png");
        if (obstaculoTx != null) obstaculoTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        cajaTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-05.png");
        if (cajaTx != null) cajaTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        try {
            cargarDesdeJson("escenarios/prueba_1.json");
        } catch (FileNotFoundException e) {
            Gdx.app.log("Escenario", "JSON no encontrado, usando fallback");
            construirFallback();
            onPostCargar();
        }
    }

    private void construirFallback() {
        plataformas.add(new Plataforma(0, 0, 320, 32, plataformaTx));
        plataformas.add(new Plataforma(400, 120, 320, 32, plataformaTx));
        plataformas.add(new Plataforma(800, 240, 320, 32, plataformaTx));
        trampolines.add(new Trampolin(500, 120, 96, 24, 520f, trampolinTx));
        obstaculos.add(new Obstaculo(650, 120, 64, 64, obstaculoTx));
        obstaculos.add(new Obstaculo(950, 240, 64, 64, obstaculoTx));
        cajasArmas.add(new CajaArmas(820, 280, 48, 48, cajaTx));
        clima = Clima.NEON;
    }

    @Override
    protected void onPostCargar() {
        Gdx.app.log("Escenario", "Clima: " + clima);
        Gdx.app.log("Escenario", "# plataformas: " + plataformas.size);
        Gdx.app.log("Escenario", "# trampolines: " + trampolines.size);
        Gdx.app.log("Escenario", "# obstaculos: " + obstaculos.size);
        Gdx.app.log("Escenario", "# cajas: " + cajasArmas.size);
    }

    @Override
    public void dibujar(SpriteBatch batch) {
        if (fondo != null) {
            batch.draw(fondo, 0, 0, GameScreen.WORLD_W, GameScreen.WORLD_H);
        } else {
            Gdx.app.log("Escenario", "WARN textura fondo faltante");
            batch.end();
            bgDebug.setProjectionMatrix(batch.getProjectionMatrix());
            bgDebug.begin(ShapeRenderer.ShapeType.Filled);
            bgDebug.setColor(Color.DARK_GRAY);
            bgDebug.rect(0, 0, GameScreen.WORLD_W, GameScreen.WORLD_H);
            bgDebug.end();
            batch.begin();
        }
        super.dibujar(batch);
        if (clima == Clima.NEON) {
            Color old = batch.getColor();
            batch.setColor(1f, 0.2f, 0.8f, 0.15f);
            batch.draw(pixelBlanco, 0, 0, GameScreen.WORLD_W, GameScreen.WORLD_H);
            batch.setColor(old);
        }
    }
}
