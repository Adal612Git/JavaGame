package com.mygdx.runner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.runner.GameMain;
import com.mygdx.runner.characters.AiController;
import com.mygdx.runner.characters.CharacterBase;
import com.mygdx.runner.characters.PlayerController;
import com.mygdx.runner.graphics.ParallaxBackground;
import com.mygdx.runner.world.Track;

import java.util.HashMap;
import java.util.Map;

/**
 * Race screen with basic physics and NPCs.
 */
public class RaceScreen implements Screen {
    private final GameMain game;
    private final String playerId;
    private SpriteBatch batch;
    private Texture pixel;
    private BitmapFont font;
    private OrthographicCamera camera;
    private ParallaxBackground bg;
    private Track track;
    private CharacterBase player,npc1,npc2;
    private PlayerController playerCtrl;
    private AiController ai1,ai2;
    private float countdown=3f;
    private boolean started=false;
    private boolean raceFinished=false;
    private float time;
    private Map<CharacterBase,Float> finishTimes = new HashMap<>();

    // artifacts
    private enum ArtifactType {
        CAJA(50, "caja"), ESCUDO(60, "escudo"), MOCHILA(70, "mochila"),
        PISTOLA(80, "pistola"), TRUENO(90, "trueno"), TURBO(100, "turbo");
        final int points; final String file;
        ArtifactType(int p,String f){points=p;file=f;}
    }

    private static class Artifact {
        ArtifactType type; float x,y,width,height; TextureRegion region; int points;
        final Rectangle bounds = new Rectangle(); boolean active;
    }

    private static class FloatingText {
        String text; float x,y,time;
    }

    private Array<Artifact> artifacts;
    private Pool<Artifact> artifactPool;
    private Array<FloatingText> floatingTexts;
    private Pool<FloatingText> floatPool;
    private Map<ArtifactType,TextureRegion> artRegions;
    private int score;
    private boolean transitionLock;
    private float finishDelay;
    private long spawnSeed;
    private AssetManager assetManager;

    public RaceScreen(GameMain game, String playerId) {
        this.game = game;
        this.playerId = playerId;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        pixel = new Texture(pm); pm.dispose();
        camera = new OrthographicCamera(640, 360);
        camera.position.set(320,180,0);
        assetManager = game.getAssetManager();
        bg = new ParallaxBackground(assetManager, camera.viewportWidth, camera.viewportHeight);
        track = new Track();
        // characters
        player = new CharacterBase(playerId, 0, assetManager, 190f, 6f);
        String[] all = {"orion","roky","thumper"};
        java.util.List<String> npcs = new java.util.ArrayList<>();
        for(String s: all) if(!s.equals(playerId)) npcs.add(s);
        npc1 = new CharacterBase(npcs.get(0), -40, assetManager, 205f, 6f);
        npc2 = new CharacterBase(npcs.get(1), -80, assetManager, 205f, 6f);
        playerCtrl = new PlayerController(player);
        ai1 = new AiController(npc1, track, track.getNpcMin(), track.getNpcMax());
        ai2 = new AiController(npc2, track, track.getNpcMin(), track.getNpcMax());
        Gdx.app.log("INFO", "Scale factor visual = 1.55");
        // artifacts
        artRegions = new java.util.EnumMap<>(ArtifactType.class);
        for (ArtifactType t : ArtifactType.values()) {
            Texture tex = assetManager.get("assets/images/artefactos/" + t.file + ".png", Texture.class);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            artRegions.put(t, new TextureRegion(tex));
        }
        artifacts = new Array<>();
        artifactPool = new Pool<Artifact>() { @Override protected Artifact newObject(){return new Artifact();} };
        floatingTexts = new Array<>();
        floatPool = new Pool<FloatingText>() { @Override protected FloatingText newObject(){return new FloatingText();} };
        spawnSeed = System.currentTimeMillis();
        spawnArtifacts();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f,0.1f,0.1f,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (raceFinished) {
            finishDelay -= delta;
            if (finishDelay < 0f) finishDelay = 0f;
            if (!transitionLock && finishDelay == 0f) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { restartRace(); return; }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { exitToSelect(); return; }
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !transitionLock) {
            exitToSelect();
            return;
        }

        if(!started){
            countdown -= delta;
            if(countdown<=0){started=true;}
        } else if(!raceFinished){
            time += delta;
            playerCtrl.update(delta);
            ai1.update(delta); ai2.update(delta);
            player.update(delta, track);
            npc1.update(delta, track);
            npc2.update(delta, track);
            checkFinish(player);
            checkFinish(npc1);
            checkFinish(npc2);
            updateArtifacts(delta);
        }

        camera.position.x = player.position.x + 150;
        float halfW = camera.viewportWidth / 2f;
        if(camera.position.x < halfW) camera.position.x = halfW;
        if(camera.position.x > track.getFinishX()) camera.position.x = track.getFinishX();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        bg.render(batch, camera.position.x, camera.viewportWidth, camera.viewportHeight);
        // ground
        batch.setColor(Color.DARK_GRAY);
        batch.draw(pixel, camera.position.x - halfW, track.getGroundY()-5, camera.viewportWidth,5);
        // start line
        batch.setColor(Color.WHITE);
        batch.draw(pixel,0,track.getGroundY(),5,50);
        // finish line
        batch.setColor(Color.YELLOW);
        batch.draw(pixel,track.getFinishX(),track.getGroundY(),5,50);
        // obstacles
        batch.setColor(Color.BROWN);
        for(Rectangle r: track.getObstacles()){
            batch.draw(pixel,r.x,r.y,r.width,r.height);
        }
        batch.setColor(Color.WHITE);
        for(Artifact a: artifacts){
            if(a.active){
                batch.draw(a.region, a.x, a.y, a.width, a.height);
            }
        }
        // characters
        player.render(batch);
        npc1.render(batch);
        npc2.render(batch);
        for(FloatingText ft: floatingTexts){
            font.draw(batch, ft.text, ft.x, ft.y);
        }
        batch.end();

        // HUD
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if(!started){
            int count = (int)Math.ceil(countdown);
            String msg = count>0?String.valueOf(count):"GO!";
            font.draw(batch,msg,camera.position.x-10,camera.position.y+100);
        }
        Array<CharacterBase> arr = new Array<>();
        arr.add(player); arr.add(npc1); arr.add(npc2);
        arr.sort((a,b)->Float.compare(b.position.x,a.position.x));
        float y = camera.position.y+160;
        for(int i=0;i<arr.size;i++){
            font.draw(batch,(i+1)+") "+arr.get(i).getName(),camera.position.x-300,y); y-=20;}
        font.draw(batch, "SCORE: " + score, camera.position.x-300, camera.position.y+170);
        if(raceFinished){
            font.draw(batch,"Ganador: "+winnerName(),camera.position.x-80,camera.position.y+40);
            font.draw(batch,"R para reiniciar, ESC para menu",camera.position.x-120,camera.position.y+20);
        }
        batch.end();
    }

    private void checkFinish(CharacterBase c){
        if(!finishTimes.containsKey(c) && c.position.x >= track.getFinishX()){
            finishTimes.put(c,time);
            Gdx.app.log("INFO","Finish "+c.getName()+" t="+String.format("%.2f",time));
            if(finishTimes.size()==3){
                raceFinished=true;
                finishDelay = 0.3f;
                java.util.List<Map.Entry<CharacterBase,Float>> list = new java.util.ArrayList<>(finishTimes.entrySet());
                list.sort(java.util.Comparator.comparing(Map.Entry::getValue));
                StringBuilder sb = new StringBuilder("Order: ");
                for(int i=0;i<list.size();i++){
                    sb.append((i+1)).append(")").append(list.get(i).getKey().getName()).append(" ");
                }
                Gdx.app.log("INFO",sb.toString());
            }
        }
    }

    private String winnerName(){
        CharacterBase win = null; float best=Float.MAX_VALUE;
        for(Map.Entry<CharacterBase,Float> e: finishTimes.entrySet()){
            if(e.getValue()<best){best=e.getValue();win=e.getKey();}
        }
        return win!=null?win.getName():"";
    }

    @Override public void resize(int width,int height){
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(width/2f, height/2f, 0);
        camera.update();
    }
    @Override public void pause(){}
    @Override public void resume(){}
    @Override
    public void hide(){
        Timer.instance().clear();
        Gdx.input.setInputProcessor(null);
        track.getObstacles().clear();
        artifacts.clear();
        floatingTexts.clear();
        if (bg != null) bg.clear();
    }
    @Override public void dispose(){
        Timer.instance().clear();
        batch.dispose();
        pixel.dispose();
        font.dispose();
        if (bg != null) bg.dispose();
        player.dispose();
        npc1.dispose();
        npc2.dispose();
        artifacts.clear();
        floatingTexts.clear();
        if (bg != null) bg.clear();
        Gdx.app.log("INFO","RaceScreen.dispose(): stage/world cleared, timers canceled, inputs removed");
    }

    private void spawnArtifacts(){
        java.util.Random rng = new java.util.Random(spawnSeed);
        float x = 500f;
        int idx = 0;
        while (x < track.getFinishX() - 400f && idx < 6) {
            ArtifactType t = ArtifactType.values()[idx % ArtifactType.values().length];
            Artifact a = artifactPool.obtain();
            a.type = t; a.points = t.points; a.region = artRegions.get(t);
            a.width = 32f; a.height = 32f; a.x = x; a.y = track.getGroundY() + (idx %2==0?10f:60f);
            a.bounds.set(a.x, a.y, a.width, a.height); a.active = true;
            artifacts.add(a);
            x += 700f + rng.nextFloat()*250f; // 700-950
            idx++;
        }
        Gdx.app.log("INFO","Artifacts: spawned " + artifacts.size + " items, seed=" + spawnSeed);
    }

    private void updateArtifacts(float delta){
        Rectangle playerRect = player.getBounds();
        for(int i=artifacts.size-1;i>=0;i--){
            Artifact a = artifacts.get(i);
            if(!a.active) continue;
            if(playerRect.overlaps(a.bounds)){
                score += a.points;
                FloatingText ft = floatPool.obtain();
                ft.text = "+" + a.points;
                ft.x = player.position.x; ft.y = player.position.y + 80f; ft.time = 0.6f;
                floatingTexts.add(ft);
                a.active=false; artifacts.removeIndex(i); artifactPool.free(a);
                Gdx.app.log("INFO","Collected: " + a.type.file + " +" + a.points + " (total=" + score + ")");
            }
        }
        for(int i=floatingTexts.size-1;i>=0;i--){
            FloatingText ft = floatingTexts.get(i);
            ft.y += 30f * delta;
            ft.time -= delta;
            if(ft.time<=0){floatingTexts.removeIndex(i); floatPool.free(ft);} }
    }

    private void exitToSelect() {
        transitionLock = true;
        Gdx.input.setInputProcessor(null);
        Timer.instance().clear();
        artifacts.clear();
        floatingTexts.clear();
        Gdx.app.postRunnable(() -> game.setScreen(new SelectScreen(game, assetManager)));
    }

    private void restartRace() {
        transitionLock = true;
        Gdx.input.setInputProcessor(null);
        Timer.instance().clear();
        artifacts.clear();
        floatingTexts.clear();
        Gdx.app.postRunnable(() -> game.setScreen(new RaceScreen(game, playerId)));
    }
}
