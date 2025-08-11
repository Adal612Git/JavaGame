package com.juegodiego.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.juegodiego.Const;

/**
 * Clase base de personajes.
 */
public abstract class Personaje {
    public enum Estado {IDLE, RUN, JUMP, FALL, ATTACK, HURT, DEAD}
    public enum Direccion {LEFT, RIGHT}

    protected final String id;
    protected final String nombre;
    protected final Vector2 position = new Vector2();
    protected final Vector2 velocity = new Vector2();
    protected float speed;
    protected float jumpForce;
    protected int maxHP;
    protected int hp;
    protected int attackPower;
    protected boolean onGround;
    protected Direccion dir = Direccion.RIGHT;
    protected Estado estado = Estado.IDLE;
    protected final ObjectMap<String, Float> cooldowns = new ObjectMap<>();
    protected final ObjectMap<Estado, Animation<TextureRegion>> anims = new ObjectMap<>();
    protected final Array<Texture> ownedTextures = new Array<>();

    protected boolean invulnerable;
    private float attackTimer;
    private float stateTime;

    protected Personaje(String id, String nombre, AssetManager manager, Vector2 spawn) {
        this.id = id;
        this.nombre = nombre;
        this.position.set(spawn);
        loadAnims(manager);
    }

    protected void loadAnims(AssetManager manager) {
        anims.put(Estado.IDLE, loadAnim(manager, "idle.png", Color.GREEN));
        anims.put(Estado.RUN, loadAnim(manager, "run.png", Color.BLUE));
        anims.put(Estado.JUMP, loadAnim(manager, "jump.png", Color.YELLOW));
        anims.put(Estado.FALL, loadAnim(manager, "fall.png", Color.ORANGE));
        anims.put(Estado.ATTACK, loadAnim(manager, "attack.png", Color.RED));
        anims.put(Estado.HURT, loadAnim(manager, "hurt.png", Color.PURPLE));
        anims.put(Estado.DEAD, loadAnim(manager, "dead.png", Color.GRAY));
    }

    private Animation<TextureRegion> loadAnim(AssetManager manager, String file, Color color) {
        String path = "images/personajes/animaciones/" + file;
        Texture tex;
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, Texture.class);
            manager.finishLoadingAsset(path);
            tex = manager.get(path, Texture.class);
        } else {
            int w = 64 * 4;
            int h = 64;
            com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(w, h, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pm.setColor(color);
            pm.fill();
            tex = new Texture(pm);
            pm.dispose();
            ownedTextures.add(tex);
        }
        TextureRegion[][] tmp = TextureRegion.split(tex, tex.getWidth() / 4, tex.getHeight());
        TextureRegion[] frames = tmp[0];
        return new Animation<>(0.1f, frames);
    }

    public void update(float delta) {
        handleInput(delta);
        updateCooldowns(delta);
        applyPhysics(delta);
        updateEstado();
        if (estado == Estado.ATTACK) {
            attackTimer -= delta;
            if (attackTimer <= 0) {
                setEstado(onGround ? (velocity.x == 0 ? Estado.IDLE : Estado.RUN)
                        : (velocity.y > 0 ? Estado.JUMP : Estado.FALL));
            }
        }
        stateTime += delta;
    }

    protected void handleInput(float delta) {
        // por defecto vacío
    }

    protected void updateCooldowns(float delta) {
        for (ObjectMap.Entry<String, Float> e : cooldowns.entries()) {
            if (e.value > 0) {
                e.value -= delta;
                if (e.value < 0) e.value = 0f;
            }
        }
    }

    protected void applyPhysics(float delta) {
        velocity.y += Const.GRAVITY * delta;
        if (velocity.y < Const.MAX_FALL_SPEED) {
            velocity.y = Const.MAX_FALL_SPEED;
        }
        position.mulAdd(velocity, delta);
        if (position.y <= 0) {
            position.y = 0;
            onGround = true;
            velocity.y = 0;
        } else {
            onGround = false;
        }
    }

    protected void updateEstado() {
        if (estado == Estado.ATTACK || estado == Estado.HURT || estado == Estado.DEAD) {
            return;
        }
        if (!onGround) {
            setEstado(velocity.y > 0 ? Estado.JUMP : Estado.FALL);
        } else if (velocity.x != 0) {
            setEstado(Estado.RUN);
        } else {
            setEstado(Estado.IDLE);
        }
    }

    protected void setEstado(Estado nuevo) {
        if (estado != nuevo) {
            estado = nuevo;
            Gdx.app.log(nombre, "Estado: " + nuevo);
        }
    }

    public void moveLeft() {
        velocity.x = -speed;
        dir = Direccion.LEFT;
    }

    public void moveRight() {
        velocity.x = speed;
        dir = Direccion.RIGHT;
    }

    public void stop() {
        velocity.x = 0;
    }

    public void jump() {
        if (onGround) {
            velocity.y = jumpForce;
            onGround = false;
            setEstado(Estado.JUMP);
        }
    }

    public void attack() {
        if (estado != Estado.ATTACK && estado != Estado.DEAD) {
            setEstado(Estado.ATTACK);
            attackTimer = 0.3f;
        }
    }

    public void takeDamage(int dmg) {
        if (invulnerable || estado == Estado.DEAD) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            setEstado(Estado.DEAD);
        } else {
            setEstado(Estado.HURT);
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = anims.get(estado);
        if (anim == null) return;
        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        boolean flip = dir == Direccion.LEFT;
        if (frame.isFlipX() != flip) {
            frame.flip(true, false);
        }
        batch.draw(frame, position.x, position.y);
    }

    public void setCooldown(String key, float cd) {
        cooldowns.put(key, cd);
    }

    public boolean cooldownReady(String key) {
        return !cooldowns.containsKey(key) || cooldowns.get(key) <= 0f;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        for (Texture t : ownedTextures) {
            t.dispose();
        }
    }
}
