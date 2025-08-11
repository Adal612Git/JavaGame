package com.juegodiego.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
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
    public boolean debugDrawHitbox;
    protected final AssetManager manager;

    protected boolean invulnerable;
    private float attackTimer;
    private float stateTime;

    protected Personaje(String id, String nombre, AssetManager manager, Vector2 spawn) {
        this.id = id;
        this.nombre = nombre;
        this.manager = manager;
        this.position.set(spawn);
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
            stateTime = 0f;
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

    public void renderDebug(ShapeRenderer shape) {
        if (!debugDrawHitbox) return;
        shape.rect(position.x, position.y, 32, 48);
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

    public Direccion getDir() {
        return dir;
    }

    public void setDir(Direccion d) {
        this.dir = d;
    }

    public void dispose() {
        // nada por ahora
    }
}
