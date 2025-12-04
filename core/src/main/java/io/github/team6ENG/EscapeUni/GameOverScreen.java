package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Displays then the player looses
 */
public class GameOverScreen implements Screen {

    private final Main game;

    // stage and resources created in show() and disposed in dispose()
    private Stage stage;
    private Skin skin;
    private final GlyphLayout layout = new GlyphLayout();

    private TextButton mainMenuButton;
    private TextButton exitButton;

    private com.badlogic.gdx.InputProcessor previousInputProcessor; // used to restore on hide()

    private static String titleText ;

    /**
     * Initialise game over screen
     * @param game Current instance of Main
     * @param deathMessage reason of death to display on screen
     */
    public GameOverScreen(final Main game, String deathMessage) {
        this.game = game;
        this.titleText = deathMessage;
        // DO NOT initialize stage/input here — do it in show()
    }

    @Override
    public void show() {
        // create stage with a fixed virtual size (you used 800x450)
        stage = new Stage(game.viewport);

        // remember previous input processor so we can restore it later
        previousInputProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(stage);

        // Prefer shared skin from game (do NOT dispose it later)
        skin = game.buttonSkin;

        // build UI
        setupUI();
    }
    /**
     *Add required UI elements to stage
     */
    private void setupUI() {
        exitButton = createButton("Exit");
        mainMenuButton = createButton("Main Menu");

        stage.addActor(exitButton);
        stage.addActor(mainMenuButton);

        positionButtons();
        addListeners();
    }
    /**
     * Set up each button
     * @param text buttons display text
     * @return new button with required parameters
     */
    private TextButton createButton(String text) {
        // If skin is null, fallback to a simple TextButton may fail; ensure game.buttonSkin exists in assets
        TextButton button = new TextButton(text, skin);
        button.getLabel().setFontScale(1.6f);
        button.pad(25f);
        button.setSize(320, 100);
        button.setColor(new Color(0.0f, 0.95f, 0.95f, 1f));
        return button;
    }
    /**
     * Place buttons on screen
     */
    private void positionButtons() {
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();

        mainMenuButton.setPosition((w - mainMenuButton.getWidth()) / 2f, h / 2f -40);
        exitButton.setPosition((w - exitButton.getWidth()) / 2f, h / 2f - 150);
    }
    /**
     * Add listeners for button functionality
     */
    private void addListeners() {
        Color normalColor = new Color(0.0f, 0.95f, 0.95f, 1f);
        Color clickColor = new Color(0.4f, 1f, 1f, 1f);


        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitButton.setColor(clickColor);
                Gdx.app.postRunnable(Gdx.app::exit);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                exitButton.setColor(clickColor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                exitButton.setColor(normalColor);
            }
        });
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mainMenuButton.setColor(clickColor);
                dispose();
                game.resetGame();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                mainMenuButton.setColor(clickColor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                mainMenuButton.setColor(normalColor);
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // update stage
        if (stage != null) {
            stage.act(delta);
        }

        // Draw background + title using game's batch (aligned to stage camera)
        if (stage != null) {
            game.batch.setProjectionMatrix(stage.getCamera().combined);
        } else {
            game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        }

        game.batch.begin();
        float w = (stage != null) ? stage.getViewport().getWorldWidth() : game.viewport.getWorldWidth();
        float h = (stage != null) ? stage.getViewport().getWorldHeight() : game.viewport.getWorldHeight();



        float brightness = 0.85f + 0.15f * (float) Math.sin(TimeUtils.millis() / 500f);
        if (game.menuFont != null) {
            game.menuFont.setColor(brightness, brightness, brightness, 1f);
            layout.setText(game.menuFont, titleText);
            game.menuFont.draw(game.batch, titleText, (w - layout.width) / 2f, h * 0.82f);
            game.menuFont.setColor(Color.WHITE);
        }

        game.batch.end();

        if (stage != null) stage.draw();

        // allow quick keyboard start (space)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Gdx.app.postRunnable(() -> game.setScreen(new CharacterSelectScreen(game)));
        }
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
        positionButtons();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }

        if (previousInputProcessor != null) {
            Gdx.input.setInputProcessor(previousInputProcessor);
            previousInputProcessor = null;
        }
    }

    @Override
    public void dispose() {
        // Dispose only things we created here
        if (stage != null) {
            stage.dispose();
            stage = null;
        }


        // DO NOT dispose game.menuFont or game.buttonSkin or game.batch here
    }
}
