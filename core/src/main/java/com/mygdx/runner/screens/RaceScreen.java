package com.mygdx.runner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
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
        bg = new ParallaxBackground();
        track = new Track();
        // create characters
        player = new CharacterBase(playerId, 0, 200f, 6f);
        String[] all = {"orion","roky","thumper"};
        java.util.List<String> npcs = new java.util.ArrayList<>();
        for(String s: all) if(!s.equals(playerId)) npcs.add(s);
        npc1 = new CharacterBase(npcs.get(0), -40, 210f, 6f);
        npc2 = new CharacterBase(npcs.get(1), -80, 210f, 6f);
        playerCtrl = new PlayerController(player);
        ai1 = new AiController(npc1, track, track.getNpcMin(), track.getNpcMax());
        ai2 = new AiController(npc2, track, track.getNpcMin(), track.getNpcMax());
        Gdx.app.log("INFO", "Player maxSpeedX=200 accelX=800 frictionX=6");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f,0.1f,0.1f,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new SelectScreen(game));
            return;
        }
        if (raceFinished && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new RaceScreen(game, playerId));
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
        }

        camera.position.x = player.position.x + 150;
        float halfW = camera.viewportWidth / 2f;
        if(camera.position.x < halfW) camera.position.x = halfW;
        if(camera.position.x > track.getFinishX()) camera.position.x = track.getFinishX();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        bg.render(batch,camera.position.x - halfW, camera.viewportWidth, camera.viewportHeight);
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
        for(com.badlogic.gdx.math.Rectangle r: track.getObstacles()){
            batch.draw(pixel,r.x,r.y,r.width,r.height);
        }
        // characters
        player.render(batch);
        npc1.render(batch);
        npc2.render(batch);
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
    @Override public void hide(){}
    @Override public void dispose(){
        batch.dispose();
        pixel.dispose();
        font.dispose();
        bg.dispose();
        player.dispose();
        npc1.dispose();
        npc2.dispose();
    }
}
