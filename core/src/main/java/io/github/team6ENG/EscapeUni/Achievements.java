package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Achievements {

    private final Main game;

    public class Achievement {
        String name;
        boolean unlocked;
        float displayTimer; // for showing in middle of screen temporarily
        int scoreReward;

        public Achievement(String name, int scoreReward) {
            this.name = name;
            this.unlocked = false;
            this.displayTimer = 0f;
            this.scoreReward = scoreReward;
        }
    }

    public Array<Achievement> achievements = new Array<>();
    private BitmapFont fontLarge;
    private BitmapFont fontSmall;

    public Achievements(Main game, BitmapFont fontLarge, BitmapFont fontSmall) {
        this.game = game;
        this.fontLarge = fontLarge;
        this.fontSmall = fontSmall;

        // Initialize achievements
        achievements.add(new Achievement("Hidden Master", 15)); // finding all hidden events
        achievements.add(new Achievement("Going Swimming", 0));  // whale in water
        achievements.add(new Achievement("Speedy Finish", 50));  // finishing <2.5 min
    }

    public Array<Achievement> getAll() {
        return achievements;
    }

    // Unlock achievement by name
    public void unlock(String name) {
        for (Achievement a : achievements) {
            if (a.name.equals(name) && !a.unlocked) {
                a.unlocked = true;
                a.displayTimer = 3f; // show in middle of screen for 3 seconds

                // Only add score if game is not null
                if (game != null) {
                    game.score += a.scoreReward;
                }
            }
        }
    }

    // Update display timers
    public void update(float delta) {
        for (Achievement a : achievements) {
            if (a.displayTimer > 0f) {
                a.displayTimer -= delta;
            }
        }
    }

    // Render achievements

    public void render(SpriteBatch batch, float worldWidth, float worldHeight) {
        // Drawing temporarily unlocked in middle of screen
        for (Achievement a : achievements) {
            if (a.displayTimer > 0f) {
                String text = "Achievement Unlocked: " + a.name;
                GlyphLayout layout = new GlyphLayout(fontLarge, text); // measure text width/height
                float x = (worldWidth - layout.width) / 2f;
                float y = (worldHeight + layout.height) / 2f;

                fontLarge.setColor(Color.GOLD);
                fontLarge.draw(batch, layout, x, y);
            }
        }

        // Drawing permanently unlocked achievements
        String title = "Achievements:";
        GlyphLayout titleLayout = new GlyphLayout(fontSmall, title);
        float titleX = 20f; // left margin
        float titleY = worldHeight - 250f; // top margin

        fontSmall.setColor(Color.GOLD);
        fontSmall.draw(batch, titleLayout, titleX, titleY);

        float spacing = 25f; // space between each achievement
        float offsetBelowTitle = 20f; // extra gap between title and first achievement

        for (int i = 0; i < achievements.size; i++) {
            Achievement a = achievements.get(i);
            if (a.unlocked) {
                float y = titleY - titleLayout.height - offsetBelowTitle - spacing * i;
                fontSmall.draw(batch, a.name, titleX, y);
            }
        }

    }


    // Check if achievement unlocked
    public boolean isUnlocked(String name) {
        for (Achievement a : achievements) {
            if (a.name.equals(name)) return a.unlocked;
        }
        return false;
    }
}
