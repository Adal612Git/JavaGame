package com.juegDiego.core.escenarios;

import com.badlogic.gdx.Gdx;
import java.io.FileNotFoundException;

/**
 * Implementación sencilla de un escenario para pruebas.
 */
public class EscenarioPrueba extends Escenario {
    public EscenarioPrueba() {
        try {
            cargarDesdeJson("assets/escenarios/prueba_1.json");
        } catch (FileNotFoundException e) {
            Gdx.app.log("Escenario", "JSON no encontrado, usando fallback");
            construirFallback();
            onPostCargar();
        }
    }

    private void construirFallback() {
        plataformas.add(new Plataforma(0, 0, 320, 32, placeholder));
        plataformas.add(new Plataforma(400, 120, 320, 32, placeholder));
        plataformas.add(new Plataforma(800, 240, 320, 32, placeholder));
        trampolines.add(new Trampolin(500, 120, 64, 32, 600f, placeholder));
        obstaculos.add(new Obstaculo(650, 120, 32, 32, placeholder));
        obstaculos.add(new Obstaculo(950, 240, 32, 32, placeholder));
        cajasArmas.add(new CajaArmas(820, 280, 32, 32, placeholder));
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
}
