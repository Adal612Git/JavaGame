package com.mygdx.runner.screens.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.runner.GameMain;
import com.mygdx.runner.screens.RaceScreen;

/**
 * Lightweight loading screen for race assets.
 */
public class RaceLoadingScreen implements Screen {
    private final GameMain game; private final String playerId;
    private SpriteBatch batch; private BitmapFont font; private Texture pixel;
    private AssetManager am; private boolean queued; private boolean done;

    public RaceLoadingScreen(GameMain game, String playerId){
        this.game=game; this.playerId=playerId;
    }

    @Override
    public void show(){
        batch = new SpriteBatch();
        font = new BitmapFont();
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888); pm.setColor(Color.WHITE); pm.fill();
        pixel = new Texture(pm); pm.dispose();
        am = game.getAssetManager();
        queue();
    }

    private void queue(){
        if(queued) return; queued=true;
        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.genMipMaps=false; param.minFilter= Texture.TextureFilter.Linear; param.magFilter= Texture.TextureFilter.Linear;
        // characters
        String[] all={"orion","roky","thumper"};
        for(String id: all){
            String base="assets/images/personajes/"+id+"/";
            // ensure placeholder is available for CharacterBase
            String placeholder = base + "placeholder.png";
            if (Gdx.files.internal(placeholder).exists() && !am.isLoaded(placeholder)) {
                am.load(placeholder, Texture.class, param);
            }
            // attempt to preload a representative frame for common states
            String[] states={"idle","run","jump","fall"};
            for(String st:states){
                String path=base+st+"/1.png";
                // only queue existing files to avoid runtime errors
                if(Gdx.files.internal(path).exists() && !am.isLoaded(path)) {
                    am.load(path, Texture.class, param);
                }
            }
        }
        // scenario layers
        String base="assets/escenarios/ecenario_Ralph";
        com.badlogic.gdx.files.FileHandle dir = Gdx.files.internal(base);
        for(com.badlogic.gdx.files.FileHandle f: dir.list("png")) if(!am.isLoaded(f.path())) am.load(f.path(), Texture.class, param);
        // artifacts
        String[] arts={"caja","escudo","mochila","pistola","trueno","turbo"};
        for(String a:arts){
            String p="assets/images/artefactos/"+a+".png";
            if(!am.isLoaded(p)) am.load(p, Texture.class, param);
        }
    }

    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0,0,0,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(!done){
            boolean finished = am.update(16);
            float prog = am.getProgress();
            if(finished){
                done=true;
                Gdx.app.postRunnable(() -> game.setScreen(new RaceScreen(game, playerId)));
            }
            batch.begin();
            batch.setColor(Color.DARK_GRAY);
            batch.draw(pixel,100,160,440,20);
            batch.setColor(Color.GREEN);
            batch.draw(pixel,100,160,440*prog,20);
            batch.setColor(Color.WHITE);
            font.draw(batch,"Cargando...",100,150);
            batch.end();
        }
    }

    @Override public void resize(int width,int height){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){ Gdx.input.setInputProcessor(null); }
    @Override public void dispose(){ batch.dispose(); font.dispose(); pixel.dispose(); }
}

