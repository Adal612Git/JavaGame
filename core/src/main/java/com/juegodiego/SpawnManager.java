package com.juegodiego;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.juegDiego.core.escenarios.Escenario;
import com.juegDiego.core.escenarios.Plataforma;
import com.juegodiego.personajes.Personaje;

import java.util.EnumMap;
import java.util.Random;

/**
 * Gestiona la aparición de artefactos y cajas en las plataformas seguras.
 */
public class SpawnManager {

    public enum ArtefactoTipo {TURBO, TRUENO, ESCUDO, PISTOLA, MOCHILA, CAJA}

    private static class SpawnItem {
        ArtefactoTipo tipo;
        Rectangle drawBounds;
        Rectangle pickupBounds;
        Texture texture;
        Plataforma plataforma;
    }

    private final Escenario escenario;
    private final Array<SpawnItem> items = new Array<>();
    private final EnumMap<ArtefactoTipo, Texture> textures = new EnumMap<>(ArtefactoTipo.class);
    private final Random rng = new Random();
    private Plataforma main, sec1, sec2;
    private float tileSize = 48f;
    private float respawnTimer;

    public SpawnManager(Escenario escenario) {
        this.escenario = escenario;
        seleccionarPlataformas();
        inicializarEscala();
        spawnsIniciales();
        scheduleRespawn();
    }

    private void seleccionarPlataformas() {
        Array<Plataforma> plats = new Array<>(escenario.getPlataformas());
        plats.sort((a, b) -> Float.compare(b.getBounds().width, a.getBounds().width));
        if (plats.size > 0) main = plats.get(0);
        if (plats.size > 1) sec1 = plats.get(1);
        if (plats.size > 2) sec2 = plats.get(2);
    }

    private void inicializarEscala() {
        if (main != null) {
            tileSize = main.getBounds().height;
        }
    }

    private Texture getTexture(ArtefactoTipo t) {
        Texture tex = textures.get(t);
        if (tex == null) {
            String name = "";
            switch (t) {
                case TURBO: name = "turbo"; break;
                case TRUENO: name = "trueno"; break;
                case ESCUDO: name = "escudo"; break;
                case PISTOLA: name = "pistola"; break;
                case MOCHILA: name = "mochila"; break;
                case CAJA: name = "caja"; break;
            }
            tex = new Texture(Gdx.files.internal("images/artefactos/" + name + ".png"));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textures.put(t, tex);
        }
        return tex;
    }

    private void spawnsIniciales() {
        if (main == null) return;
        // escudo -> centro de MAIN
        spawnSobrePlataforma(ArtefactoTipo.ESCUDO, main, (main.getBounds().width - drawSize()) / 2f, "MAIN");
        // pistola -> borde derecho de SECONDARY_2 (-1 tile)
        if (sec2 != null)
            spawnSobrePlataforma(ArtefactoTipo.PISTOLA, sec2, sec2.getBounds().width - tileSize - drawSize(), "SECONDARY_2");
        // turbo -> borde izquierdo de SECONDARY_1 (+1 tile)
        if (sec1 != null)
            spawnSobrePlataforma(ArtefactoTipo.TURBO, sec1, tileSize, "SECONDARY_1");
        // trueno -> centro de SECONDARY_2
        if (sec2 != null)
            spawnSobrePlataforma(ArtefactoTipo.TRUENO, sec2, (sec2.getBounds().width - drawSize()) / 2f, "SECONDARY_2");
        // mochila -> borde derecho de SECONDARY_1 (-1 tile)
        if (sec1 != null)
            spawnSobrePlataforma(ArtefactoTipo.MOCHILA, sec1, sec1.getBounds().width - tileSize - drawSize(), "SECONDARY_1");
        // cajas -> MAIN 25%, 50%, 75%
        float[] perc = {0.25f, 0.5f, 0.75f};
        for (float p : perc) {
            float x = main.getBounds().width * p - drawSize() / 2f;
            spawnSobrePlataforma(ArtefactoTipo.CAJA, main, x, "MAIN");
        }
    }

    private float drawSize() {
        return tileSize * 0.8f;
    }

    private float pickupSize() {
        return drawSize() * 0.7f;
    }

    private void spawnSobrePlataforma(ArtefactoTipo tipo, Plataforma plat, float relX, String label) {
        float w = drawSize();
        float h = drawSize();
        float x = plat.getBounds().x + relX;
        x = Math.max(plat.getBounds().x, Math.min(x, plat.getBounds().x + plat.getBounds().width - w));
        float y = plat.getBounds().y + plat.getBounds().height + 2f;
        if (y <= 0f) {
            x = plat.getBounds().x + (plat.getBounds().width - w) / 2f;
            y = plat.getBounds().y + plat.getBounds().height + 2f;
            Gdx.app.log("WARN", "Relocated spawn off hazard -> (" + x + "," + y + ")");
        }
        SpawnItem item = new SpawnItem();
        item.tipo = tipo;
        item.drawBounds = new Rectangle(x, y, w, h);
        float ps = pickupSize();
        item.pickupBounds = new Rectangle(x + (w - ps) / 2f, y + (h - ps) / 2f, ps, ps);
        item.texture = getTexture(tipo);
        item.plataforma = plat;
        items.add(item);
        Gdx.app.log("INFO", "Spawn " + tipo + " @ (" + x + "," + y + ") on " + label);
    }

    private void scheduleRespawn() {
        respawnTimer = 15f + rng.nextFloat() * 10f;
    }

    public void update(float delta, Personaje player, ScoreManager score) {
        for (int i = items.size - 1; i >= 0; i--) {
            SpawnItem it = items.get(i);
            if (it.pickupBounds.overlaps(player.getBounds())) {
                handlePickup(it, score, player);
                items.removeIndex(i);
            }
        }
        respawnTimer -= delta;
        if (respawnTimer <= 0f) {
            int count = 0;
            for (SpawnItem it : items) if (it.tipo != ArtefactoTipo.CAJA) count++;
            if (count < 3) {
                ArtefactoTipo[] opts = {ArtefactoTipo.TURBO, ArtefactoTipo.TRUENO, ArtefactoTipo.ESCUDO, ArtefactoTipo.PISTOLA, ArtefactoTipo.MOCHILA};
                ArtefactoTipo t = opts[rng.nextInt(opts.length)];
                Plataforma plat = pickRandomPlatform();
                float w = drawSize();
                float x = plat.getBounds().x + rng.nextFloat() * (plat.getBounds().width - w);
                spawnSobrePlataforma(t, plat, x - plat.getBounds().x, labelFor(plat));
            }
            scheduleRespawn();
        }
    }

    private Plataforma pickRandomPlatform() {
        int n = (sec1 != null ? 1 : 0) + (sec2 != null ? 1 : 0) + 1;
        int idx = rng.nextInt(n);
        if (idx == 0) return main;
        if (idx == 1) return sec1;
        return sec2;
    }

    private String labelFor(Plataforma p) {
        if (p == main) return "MAIN";
        if (p == sec1) return "SECONDARY_1";
        if (p == sec2) return "SECONDARY_2";
        return "UNKNOWN";
    }

    private void handlePickup(SpawnItem it, ScoreManager score, Personaje player) {
        switch (it.tipo) {
            case TURBO:
                score.pickupTurbo();
                if (escenario != null) {
                    float dur = score.isMochilaActive() ? 15f : 10f;
                    escenario.rampaVelocidad(player, 1.15f, dur);
                }
                break;
            case TRUENO:
                score.pickupTrueno();
                break;
            case ESCUDO:
                score.pickupEscudo();
                break;
            case PISTOLA:
                score.pickupPistola();
                break;
            case MOCHILA:
                score.pickupMochila();
                break;
            case CAJA:
                score.breakBox();
                if (rng.nextFloat() < 0.15f) {
                    ArtefactoTipo t = rng.nextBoolean() ? ArtefactoTipo.TURBO : ArtefactoTipo.TRUENO;
                    spawnSobrePlataforma(t, it.plataforma, it.drawBounds.x - it.plataforma.getBounds().x, labelFor(it.plataforma));
                }
                break;
        }
    }

    public void render(SpriteBatch batch) {
        if (items.size == 0) return;
        batch.setColor(1f,1f,1f,1f);
        for (SpawnItem it : items) {
            batch.draw(it.texture, it.drawBounds.x, it.drawBounds.y, it.drawBounds.width, it.drawBounds.height);
        }
        batch.setColor(1f,1f,1f,1f);
    }

    public void forceSpawn(ArtefactoTipo t, float x, float y) {
        Plataforma plat = main;
        if (playerOn(sec1, x, y)) plat = sec1;
        if (playerOn(sec2, x, y)) plat = sec2;
        float w = drawSize();
        SpawnItem item = new SpawnItem();
        item.tipo = t;
        item.drawBounds = new Rectangle(x, y, w, w);
        float ps = pickupSize();
        item.pickupBounds = new Rectangle(x + (w - ps) / 2f, y + (w - ps) / 2f, ps, ps);
        item.texture = getTexture(t);
        item.plataforma = plat;
        items.add(item);
    }

    private boolean playerOn(Plataforma p, float x, float y) {
        if (p == null) return false;
        Rectangle b = p.getBounds();
        return x >= b.x && x <= b.x + b.width;
    }

    public void breakNearestBox(Personaje player, ScoreManager score) {
        SpawnItem nearest = null;
        float best = Float.MAX_VALUE;
        for (SpawnItem it : items) {
            if (it.tipo != ArtefactoTipo.CAJA) continue;
            float dx = it.drawBounds.x - player.getBounds().x;
            float dy = it.drawBounds.y - player.getBounds().y;
            float d = dx*dx + dy*dy;
            if (d < best) { best = d; nearest = it; }
        }
        if (nearest != null) {
            handlePickup(nearest, score, player);
            items.removeValue(nearest, true);
        }
    }
}
