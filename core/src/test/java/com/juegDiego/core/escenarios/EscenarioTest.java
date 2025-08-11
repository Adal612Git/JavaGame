package com.juegDiego.core.escenarios;

import com.juegDiego.core.juego.Artefacto;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pruebas para utilidades de Escenario.
 */
public class EscenarioTest {

    @Test
    public void armaAleatoriaDeterministica() {
        Random rng = new Random(12345L);
        Artefacto a1 = Escenario.armaAleatoria(rng);
        Artefacto a2 = Escenario.armaAleatoria(rng);
        assertEquals(Artefacto.TURBO, a1);
        assertEquals(Artefacto.GANCHO, a2);
    }
}
