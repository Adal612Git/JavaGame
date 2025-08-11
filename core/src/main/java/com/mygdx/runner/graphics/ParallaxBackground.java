package com.mygdx.runner.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Very simple parallax background with colored stripes.
 */
public class ParallaxBackground {
    private static class Layer {
        Color color; float ratio; float stripe;
        Layer(Color c, float r, float s){color=c;ratio=r;stripe=s;}
    }
    private final Layer[] layers;

    public ParallaxBackground() {
        layers = new Layer[]{
                new Layer(new Color(0.3f,0.5f,0.8f,1f),0.2f,400f),
                new Layer(new Color(0.5f,0.8f,1f,1f),0.5f,200f)
        };
    }

    public void render(SpriteBatch batch, Texture pixel, float camX, float screenW, float screenH) {
        for (Layer l : layers) {
            batch.setColor(l.color);
            float offset = (camX * l.ratio) % l.stripe;
            for (float x = -offset; x < screenW; x += l.stripe) {
                batch.draw(pixel, x, 0, l.stripe, screenH);
            }
        }
        batch.setColor(Color.WHITE);
    }
}
