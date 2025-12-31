package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

//Health system used for damage on player
public class HealthSystem {
    private float health;
    private boolean isInvincible;
    private float invincibleTimer;
    private boolean isHit = false;
    private float hitTimer = 0.0f;

    //Health initialised at 100 for start game
    public HealthSystem() {
        this.health = 100f;
        this.isInvincible = false;
        this.invincibleTimer = 0f;
    }

    //Invincibility timer for shield
    public void update(float delta) {
        if (isInvincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0) {
                isInvincible = false;
            }
        }
        if (isHit) {
            hitTimer -= delta;
            if (hitTimer <= 0) {
                isHit = false;
            }
        }
    }

    public float getHealth() {
        return health;
    }

    //Keeps damage change different for dean and goose
    public void takeDamage(float amount) {
        if (!isInvincible) {
            health -= amount;
            if (health < 0) health = 0;
            isHit = true;
            hitTimer = 2.0f;
        }
    }

    public void heal(float amount) {
        health += amount;
    }

    //Sets timer for invincibility for 10 seconds, can be changed
    public void shieldOn() {
        isInvincible = true;
        invincibleTimer = 10f;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public boolean isHit() {return isHit;}

    public boolean isDead() {
        return health <= 0;
    }

    //Render in healthbar, different colour if shield is on
    public void render(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        float barWidth = health;
        float barHeight = 10;
        float border = 3f;

        float x = camera.position.x + (camera.viewportWidth / 2) - barWidth - 20;
        float y = camera.position.y + (camera.viewportHeight / 2) - 40;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        //Will show green if shield is activated, otherwise red typical
        if (isInvincible) {
            shapeRenderer.setColor(0, 1, 0, 1);
        } else {
            shapeRenderer.setColor(1, 0, 0, 1);
        }
        shapeRenderer.rect(x + border, y + border,
            barWidth - 2 * border,
            barHeight - 2 * border);
        shapeRenderer.end();
    }
}
