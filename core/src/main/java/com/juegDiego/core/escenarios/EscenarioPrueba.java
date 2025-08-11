package com.juegDiego.core.escenarios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
        plataformaTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-02.png");
        trampolinTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-03.png");
        obstaculoTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-04.png");
        cajaTx = getTexture("images/escenarios/ecenario_Ralph/ecenario_Ralph-05.png");
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
        trampolines.add(new Trampolin(500, 120, 64, 16, 520f, trampolinTx));
        obstaculos.add(new Obstaculo(650, 120, 32, 32, obstaculoTx));
        obstaculos.add(new Obstaculo(950, 240, 32, 32, obstaculoTx));
        cajasArmas.add(new CajaArmas(820, 280, 32, 32, cajaTx));
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
            batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } else {
            Gdx.app.log("Escenario", "WARN textura fondo faltante");
            batch.end();
            bgDebug.setProjectionMatrix(batch.getProjectionMatrix());
            bgDebug.begin(ShapeRenderer.ShapeType.Filled);
            bgDebug.setColor(Color.DARK_GRAY);
            bgDebug.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            bgDebug.end();
            batch.begin();
        }
        super.dibujar(batch);
    }
}
