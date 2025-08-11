package com.mygdx.runner.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

/**
 * Parallax background using image layers with wrapping.
 */
public class ParallaxBackground {
    private static class Layer {
        TextureRegion region; float ratio;
        Layer(TextureRegion r, float s){region=r;ratio=s;}
    }
    private final Array<Layer> layers = new Array<>();
    private final Array<Texture> textures = new Array<>();

    public ParallaxBackground() {
        String base = "assets/escenarios/ecenario_Ralph";
        FileHandle dir = Gdx.files.internal(base);
        if (!dir.exists()) {
            base = "assets/images/escenarios/ecenario_Ralph";
            dir = Gdx.files.internal(base);
            Gdx.app.log("INFO", "Escenario fallback: " + base);
        }
        Gdx.app.log("INFO", "ParallaxBackground: capas desde " + base);
        FileHandle[] files = dir.list();
        Array<FileHandle> pngs = new Array<>();
        for (FileHandle f : files) {
            if ("png".equals(f.extension())) pngs.add(f);
        }
        pngs.sort(Comparator.comparing(FileHandle::name));
        FileHandle[] sorted = pngs.toArray(FileHandle.class);
        float[] defaults = {0.2f, 0.5f, 0.8f};
        int idx = 0;
        for (FileHandle f : sorted) {
            Texture tex = new Texture(f);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textures.add(tex);
            String lower = f.name().toLowerCase();
            float ratio;
            if (lower.contains("fondo") || lower.contains("bg")) ratio = 0.2f;
            else if (lower.contains("mid") || lower.contains("middle")) ratio = 0.5f;
            else if (lower.contains("front") || lower.contains("near")) ratio = 0.8f;
            else ratio = defaults[idx++ % defaults.length];
            layers.add(new Layer(new TextureRegion(tex), ratio));
            Gdx.app.log("INFO", "Layer " + f.path() + " factor=" + ratio);
        }
        Gdx.app.log("INFO", "Escenario: usando " + base + " (" + layers.size + " capas)");
    }

    public void render(SpriteBatch batch, float camX, float screenW, float screenH) {
        for (Layer l : layers) {
            float width = l.region.getRegionWidth();
            float height = l.region.getRegionHeight();
            float scale = screenH / height;
            float drawW = width * scale;
            float offset = (camX * l.ratio) % drawW;
            if (offset < 0) offset += drawW;
            float start = -offset;
            for (float x = start; x < screenW + drawW; x += drawW) {
                batch.draw(l.region, x, 0, drawW, screenH);
            }
        }
    }

    public void dispose() {
        for (Texture t : textures) t.dispose();
    }
}
