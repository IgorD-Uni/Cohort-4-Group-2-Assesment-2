package io.github.team6ENG.EscapeUni;
import com.badlogic.gdx.math.Rectangle;

public class Student {
    public float x, y;
    public float width = 32, height = 32;
    public boolean active = true;

    private float speed;
    private int direction; // -1 = left, +1 = right

    public Student(float x, float y, float speed, int direction) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
    }

    public void update(float delta) {
        if (!active) return;

        x += direction * speed * delta;

        // Deactivate if far off screen
        if (x < -100 || x > 5000) {
            active = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
