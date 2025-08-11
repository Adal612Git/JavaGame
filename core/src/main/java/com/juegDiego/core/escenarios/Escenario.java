package com.juegDiego.core.escenarios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.juegDiego.core.juego.Artefacto;
import com.juegDiego.core.juego.Player;

import java.io.FileNotFoundException;
import java.util.Random;

/**
 * Clase base para los escenarios del juego.
 */
public abstract class Escenario {
    protected final Array<Plataforma> plataformas = new Array<>();
    protected final Array<Trampolin> trampolines = new Array<>();
    protected final Array<Obstaculo> obstaculos = new Array<>();
    protected final Array<CajaArmas> cajasArmas = new Array<>();
    protected Clima clima = Clima.SOLEADO;

    private final ObjectMap<Player, Float> boostTimers = new ObjectMap<>();
    private final ObjectMap<Player, Float> baseSpeeds = new ObjectMap<>();
    protected final Texture placeholder;

    public Escenario() {
        placeholder = new Texture("images/artefactos/caja.png");
    }

    /**
     * Carga los elementos del escenario desde un archivo JSON.
     */
    public void cargarDesdeJson(String path) throws FileNotFoundException {
        FileHandle handle = Gdx.files.internal(path);
        if (!handle.exists() || handle.length() == 0) {
            throw new FileNotFoundException("No existe archivo: " + path);
        }
        JsonValue root = new JsonReader().parse(handle);
        Texture tex = placeholder;
        JsonValue arr;
        arr = root.get("plataformas");
        if (arr != null) {
            for (JsonValue v : arr) {
                plataformas.add(new Plataforma(v.getFloat("x"), v.getFloat("y"),
                        v.getFloat("w"), v.getFloat("h"), tex));
            }
        }
        arr = root.get("trampolines");
        if (arr != null) {
            for (JsonValue v : arr) {
                trampolines.add(new Trampolin(v.getFloat("x"), v.getFloat("y"),
                        v.getFloat("w"), v.getFloat("h"), v.getFloat("impulso"), tex));
            }
        }
        arr = root.get("obstaculos");
        if (arr != null) {
            for (JsonValue v : arr) {
                obstaculos.add(new Obstaculo(v.getFloat("x"), v.getFloat("y"),
                        v.getFloat("w"), v.getFloat("h"), tex));
            }
        }
        arr = root.get("cajas");
        if (arr != null) {
            for (JsonValue v : arr) {
                cajasArmas.add(new CajaArmas(v.getFloat("x"), v.getFloat("y"),
                        v.getFloat("w"), v.getFloat("h"), tex));
            }
        }
        String c = root.getString("clima", "SOLEADO");
        clima = Clima.valueOf(c);
        onPostCargar();
    }

    /**
     * Actualiza todos los elementos del escenario.
     */
    public void actualizar(float delta) {
        for (Plataforma p : plataformas) p.update(delta);
        for (Trampolin t : trampolines) t.update(delta);
        for (Obstaculo o : obstaculos) o.update(delta);
        for (CajaArmas c : cajasArmas) c.update(delta);

        Array<Player> toRemove = new Array<>();
        for (ObjectMap.Entry<Player, Float> e : boostTimers) {
            float time = e.value - delta;
            if (time <= 0) {
                Player p = e.key;
                p.setSpeed(baseSpeeds.get(p));
                toRemove.add(p);
            } else {
                boostTimers.put(e.key, time);
            }
        }
        for (Player p : toRemove) {
            boostTimers.remove(p);
            baseSpeeds.remove(p);
        }
    }

    /**
     * Dibuja el escenario y sus elementos.
     */
    public void dibujar(SpriteBatch batch) {
        for (Plataforma p : plataformas) p.draw(batch);
        for (Trampolin t : trampolines) t.draw(batch);
        for (Obstaculo o : obstaculos) o.draw(batch);
        for (CajaArmas c : cajasArmas) c.draw(batch);

        if (clima == Clima.NEON) {
            Color prev = batch.getColor();
            batch.setColor(1f, 0f, 1f, 0.25f);
            batch.draw(placeholder, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(prev);
        }
    }

    /**
     * Obtiene los límites globales del escenario.
     */
    public Rectangle obtenerLimites() {
        Rectangle r = new Rectangle();
        for (Plataforma p : plataformas) r.merge(p.getBounds());
        for (Trampolin t : trampolines) r.merge(t.getBounds());
        for (Obstaculo o : obstaculos) r.merge(o.getBounds());
        for (CajaArmas c : cajasArmas) r.merge(c.getBounds());
        return r;
    }

    /**
     * Aplica un aumento temporal de velocidad al jugador.
     */
    public void rampaVelocidad(Player player, float factor, float duracionSeg) {
        if (!baseSpeeds.containsKey(player)) {
            baseSpeeds.put(player, player.getSpeed());
        }
        player.setSpeed(baseSpeeds.get(player) * factor);
        boostTimers.put(player, duracionSeg);
    }

    /**
     * Selecciona un artefacto al azar.
     */
    public static Artefacto armaAleatoria(Random rng) {
        Artefacto[] values = Artefacto.values();
        return values[rng.nextInt(values.length)];
    }

    public boolean intersectaPlayer(ElementoEscenario elemento, Player player) {
        return elemento.getBounds().overlaps(player.getBounds());
    }

    public Array<Trampolin> getTrampolines() {
        return trampolines;
    }

    protected abstract void onPostCargar();

    protected TextureRegion getTexture(String key) {
        return null;
    }

    public static Escenario crearEscenarioPrueba() {
        return new EscenarioPrueba();
    }
}
