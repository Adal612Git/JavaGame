package com.juegodiego.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.juegodiego.Const;
import com.juegodiego.gfx.AnimationLoader;
import java.util.EnumMap;

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
    protected final EnumMap<Estado, Animation<TextureRegion>> anims = new EnumMap<>(Estado.class);
    public boolean debugDrawHitbox;
    protected final AssetManager manager;
    private static final float EPS = 0.01f;

    protected boolean invulnerable;
    private float attackTimer;
    private float stateTime;
    private float lastDrawW;
    private float lastDrawH;
    private float lastFrameW;
    private float lastFrameH;

    protected final Rectangle bounds = new Rectangle();

    protected Personaje(String id, String nombre, AssetManager manager, Vector2 spawn) {
        this.id = id;
        this.nombre = nombre;
        this.manager = manager;
        this.position.set(spawn);
    }

    protected void ensureRunAnim() {
        Animation<TextureRegion> runAnim = anims.get(Estado.RUN);
        Animation<TextureRegion> idleAnim = anims.get(Estado.IDLE);
        if (runAnim == null || runAnim.getKeyFrames() == null || runAnim.getKeyFrames().length == 0) {
            if (idleAnim != null && idleAnim.getKeyFrames().length > 0) {
                Animation<TextureRegion> clone =
                        new Animation<>(idleAnim.getFrameDuration(), idleAnim.getKeyFrames());
                anims.put(Estado.RUN, clone);
                Gdx.app.log("[[RUN-FIX]]", "Assigned IDLE anim to RUN (frames=" + idleAnim.getKeyFrames().length + ")");
            }
        }
    }

    public void update(float delta) {
        handleInput(delta);
        updateCooldowns(delta);
        applyPhysics(delta);
        updateBounds();
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

    protected void updateBounds() {
        bounds.set(position.x, position.y, 32, 48);
    }

    protected void updateEstado() {
        if (estado == Estado.ATTACK || estado == Estado.HURT || estado == Estado.DEAD) {
            return;
        }
        if (!onGround) {
            setEstado(velocity.y > 0 ? Estado.JUMP : Estado.FALL);
        } else if (Math.abs(velocity.x) > EPS) {
            setEstado(Estado.RUN);
        } else {
            setEstado(Estado.IDLE);
        }
    }

    protected void setEstado(Estado nuevo) {
        if (estado != nuevo) {
            if (nuevo == Estado.RUN) {
                Gdx.app.log("[[STATE]]", "ENTER RUN onGround=" + onGround + " vx=" + velocity.x);
                Animation<TextureRegion> runAnim = anims.get(Estado.RUN);
                if (runAnim == null || runAnim.getKeyFrames().length == 0) {
                    Gdx.app.error("[[ERROR]]", "RUN has 0 frames after load — expected >=1");
                } else {
                    Array<String> paths = AnimationLoader.getFramePaths(id, Estado.RUN);
                    String s0 = paths != null && paths.size > 0 ? paths.get(0) : "n/a";
                    String s1 = paths != null && paths.size > 1 ? paths.get(1) : "n/a";
                    String s2 = paths != null && paths.size > 2 ? paths.get(2) : "n/a";
                    Gdx.app.log("[[RUN]]", "frames=" + runAnim.getKeyFrames().length +
                            " sample0=" + s0 + " sample1=" + s1 + " sample2=" + s2);
                }
            }
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

    public Rectangle getBounds() {
        return bounds;
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = anims.get(estado);
        if (estado == Estado.RUN && (anim == null || anim.getKeyFrames().length == 0)) {
            anim = anims.get(Estado.IDLE);
            if (anim != null) {
                Gdx.app.log("WARN", "Missing frames for RUN in " + nombre + ". Using IDLE as fallback.");
            }
        }
        if (anim == null || anim.getKeyFrames().length == 0) {
            Gdx.app.log("WARN", "Attempted to draw null texture for " + estado + "/" + id + ". Skipping.");
            return;
        }
        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        if (frame == null || frame.getTexture() == null) {
            Gdx.app.log("WARN", "Attempted to draw null texture for " + estado + "/" + id + ". Skipping.");
            return;
        }
        boolean flip = dir == Direccion.LEFT;
        if (frame.isFlipX() != flip) {
            frame.flip(true, false);
        }
        float drawHeight = Const.TARGET_SPRITE_HEIGHT;
        float drawWidth = frame.getRegionWidth() * (drawHeight / frame.getRegionHeight());
        lastFrameW = frame.getRegionWidth();
        lastFrameH = frame.getRegionHeight();
        lastDrawW = drawWidth;
        lastDrawH = drawHeight;
        batch.draw(frame, position.x, position.y, drawWidth, drawHeight);
    }

    public Estado getEstado() {
        return estado;
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

    public Vector2 getVelocity() {
        return velocity;
    }

    public Direccion getDir() {
        return dir;
    }

    public void setDir(Direccion d) {
        this.dir = d;
    }

    public float getSpeed() { return speed; }

    public void setSpeed(float s) { this.speed = s; }

    public String getNombre() { return nombre; }

    public String getId() { return id; }

    public boolean isOnGround() { return onGround; }

    public float getLastDrawWidth() { return lastDrawW; }
    public float getLastDrawHeight() { return lastDrawH; }
    public float getLastFrameWidth() { return lastFrameW; }
    public float getLastFrameHeight() { return lastFrameH; }

    public void dispose() {
        // nada por ahora
    }
}
