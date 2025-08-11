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
        final TextureRegion region; final float factor; final float w; final float h; final String name; boolean logged;
        Layer(String name, TextureRegion r, float factor){
            this.name = name; this.region = r; this.factor = factor;
            this.w = r.getRegionWidth(); this.h = r.getRegionHeight();
        }
    }

    private final Array<Layer> layers = new Array<>();
    private Texture gradientTex;

    public ParallaxBackground(AssetManager am, float viewportW, float viewportH){
        FileHandle dir = Gdx.files.internal("assets/escenarios/ecenario_Ralph");
        Array<FileHandle> files = new Array<>(dir.list("png"));
        files.sort(Comparator.comparing(FileHandle::name));
        for(FileHandle f: files){
            if(!am.isLoaded(f.path(), Texture.class)) continue;
            Texture tex = am.get(f.path(), Texture.class);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            String lower = f.name().toLowerCase();
            float factor = lower.contains("fondo")||lower.contains("bg")?0.2f:
                    lower.contains("mid")||lower.contains("middle")?0.5f:
                            lower.contains("front")||lower.contains("near")?0.8f:0.5f;
            layers.add(new Layer(f.name(), new TextureRegion(tex), factor));
            Gdx.app.log("INFO","Parallax asset: "+f.path());
        }
        if(layers.size==0){
            Pixmap pm = new Pixmap(1,256, Pixmap.Format.RGBA8888);
            for(int y=0;y<256;y++){
                float t=y/255f;
                pm.setColor(new Color(0.4f+0.3f*(1-t),0.7f+0.2f*(1-t),1f,1f));
                pm.drawPixel(0,y);
            }
            gradientTex = new Texture(pm); pm.dispose();
            gradientTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            layers.add(new Layer("gradient", new TextureRegion(gradientTex),0.2f));
        }
        Gdx.app.log("INFO","ParallaxBackground capas="+layers.size);
    }

    public void render(SpriteBatch batch, float camX, float screenW, float screenH){
        for(Layer l: layers){
            float scale = screenH / l.h;
            float drawW = l.w * scale;
            double raw = camX * l.factor;
            float nx = (float)(((raw % drawW) + drawW) % drawW);
            int tiles = (int)Math.ceil(screenW / drawW) + 3;
            float start = -nx - drawW;
            if(!l.logged){
                if(tiles < 3 || drawW <= 0f){
                    Gdx.app.log("WARN","layer="+l.name+" wDraw="+drawW+" tiles="+tiles+" factor="+l.factor);
                }else{
                    Gdx.app.log("INFO","layer="+l.name+" wDraw="+drawW+" vpw="+screenW+" tiles="+tiles+" factor="+l.factor);
                }
                l.logged = true;
            }
            for(int i=0;i<tiles+2;i++){
                batch.draw(l.region, start + i*drawW, 0, drawW, screenH);
            }
        }
    }

    public void clear(){ layers.clear(); }
    public void dispose(){ if(gradientTex!=null) gradientTex.dispose(); }
}

