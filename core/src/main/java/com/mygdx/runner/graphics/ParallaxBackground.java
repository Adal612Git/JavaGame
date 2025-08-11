package com.mygdx.runner.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

/**
 * Parallax background with manual tiling and gradient fallback.
 */
public class ParallaxBackground {
    private static class Layer {
        final TextureRegion region;
        final float factor;
        final float w;
        final float h;
        final String name;
        boolean logged;
        float scale;
        float wDraw;

        Layer(String name, TextureRegion r, float factor) {
            this.name = name;
            this.region = r;
            this.factor = factor;
            this.w = r.getRegionWidth();
            this.h = r.getRegionHeight();
        }
    }

    private final Array<Layer> layers = new Array<>();
    private Texture gradientTex;
    private float viewportW;
    private float viewportH;
    private float logAccum;
    private boolean gapThisFrame;
    private long frameCount;

    public ParallaxBackground(AssetManager am, float viewportW, float viewportH) {
        this.viewportW = viewportW;
        this.viewportH = viewportH;
        FileHandle dir = Gdx.files.internal("assets/escenarios/ecenario_Ralph");
        Array<FileHandle> files = new Array<>(dir.list("png"));
        files.sort(Comparator.comparing(FileHandle::name));
        for (FileHandle f : files) {
            if (!am.isLoaded(f.path(), Texture.class)) continue;
            Texture tex = am.get(f.path(), Texture.class);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            String lower = f.name().toLowerCase();
            float factor = lower.contains("fondo") || lower.contains("bg") ? 0.2f :
                    lower.contains("mid") || lower.contains("middle") ? 0.5f :
                            lower.contains("front") || lower.contains("near") ? 0.8f : 0.5f;
            Layer layer = new Layer(f.name(), new TextureRegion(tex), factor);
            layers.add(layer);
            Gdx.app.log("INFO", "Parallax asset: " + f.path());
        }
        if (layers.size == 0) {
            Pixmap pm = new Pixmap(1, 256, Pixmap.Format.RGBA8888);
            for (int y = 0; y < 256; y++) {
                float t = y / 255f;
                pm.setColor(new Color(0.4f + 0.3f * (1 - t), 0.7f + 0.2f * (1 - t), 1f, 1f));
                pm.drawPixel(0, y);
            }
            gradientTex = new Texture(pm);
            pm.dispose();
            gradientTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            layers.add(new Layer("gradient", new TextureRegion(gradientTex), 0.2f));
        }
        resize(viewportW, viewportH);
        Gdx.app.log("INFO", "ParallaxBackground capas=" + layers.size);
    }

    public void resize(float viewportW, float viewportH) {
        this.viewportW = viewportW;
        this.viewportH = viewportH;
        for (Layer l : layers) {
            l.scale = viewportH / l.h;
            l.wDraw = l.w * l.scale;
            Gdx.app.log("INFO", "layer=" + l.name + " regionW=" + l.w + " regionH=" + l.h +
                    " scale=" + l.scale + " wDraw=" + l.wDraw);
        }
    }

    public void render(SpriteBatch batch, float camX, float screenW, float screenH) {
        gapThisFrame = false;
        frameCount++;
        logAccum += Gdx.graphics.getDeltaTime();
        boolean doLog = false;
        if (logAccum >= 1f) {
            logAccum = 0f;
            doLog = true;
        }
        for (Layer l : layers) {
            // recompute scale in case viewport resized without explicit call
            if (l.wDraw == 0f || l.scale == 0f) {
                l.scale = screenH / l.h;
                l.wDraw = l.w * l.scale;
            }
            double raw = camX * l.factor;
            float nx = (float) (((raw % l.wDraw) + l.wDraw) % l.wDraw);
            int tiles = (int) Math.ceil(screenW / l.wDraw) + 3;
            float startX = camX - nx - l.wDraw;
            float endX = startX + (tiles + 2) * l.wDraw;
            if (!l.logged) {
                if (tiles < 3 || l.wDraw <= 0f) {
                    Gdx.app.log("WARN", "layer=" + l.name + " wDraw=" + l.wDraw + " tiles=" + tiles + " factor=" + l.factor);
                } else {
                    Gdx.app.log("INFO", "layer=" + l.name + " wDraw=" + l.wDraw + " vpw=" + screenW + " tiles=" + tiles + " factor=" + l.factor);
                }
                l.logged = true;
            }
            if (doLog) {
                Gdx.app.log("INFO", "camX=" + camX + " nx=" + nx + " tiles=" + tiles + " startX=" + startX + " endX=" + endX + " layer=" + l.name);
            }
            if (endX < camX + screenW / 2f) {
                gapThisFrame = true;
                Gdx.app.error("ERROR", "COVERAGE_GAP layer=" + l.name + " camX=" + camX + " nx=" + nx +
                        " tiles=" + tiles + " wDraw=" + l.wDraw + " vpw=" + screenW + " frame=" + frameCount);
            }
            for (int i = 0; i < tiles + 2; i++) {
                float x = startX + i * l.wDraw;
                batch.draw(l.region, x, 0, l.wDraw, screenH);
            }
        }
    }

    public boolean hasGap() {
        return gapThisFrame;
    }

    public void clear() {
        layers.clear();
    }

    public void dispose() {
        if (gradientTex != null) gradientTex.dispose();
    }
}


