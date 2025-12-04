package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * screen when game is paused, contains volume settings
 */
public class PauseScreen implements Screen {

    private final Main game;
    private final Screen playScreen;
    private final AudioManager audioManager;
    private Stage stage;
    private final Skin skin;

    private final Slider musicSlider;
    private final Slider volumeSlider;
    private final TextButton continueButton;
    private final TextButton mainMenuButton;

    /**
     * initialise pause screen
     * @param game current instance of Main
     * @param playScreen screen to return to
     * @param audioManager active audio manager
     */
    public PauseScreen(final Main game, final Screen playScreen, AudioManager audioManager) {
        this.game = game;
        this.playScreen = playScreen;
        this.audioManager = audioManager;
        this.stage = new Stage(new FitViewport(960, 540));
        this.skin = game.buttonSkin;

        audioManager.pauseMusic();
        audioManager.stopFootsteps();


        Gdx.input.setInputProcessor(stage);

        // title
        Label titleLabel = new Label("Paused", new Label.LabelStyle(game.menuFont, Color.WHITE));

        // music slider
        Label musicLabel = new Label("Music Volume", new Label.LabelStyle(game.menuFont, Color.WHITE));
        musicSlider = new Slider(0f, 1f, 0.1f, false, skin);
        musicSlider.setValue(game.musicVolume);
        // volume slider
        Label volumeLabel = new Label("Sound Volume", new Label.LabelStyle(game.menuFont, Color.WHITE));
        volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(game.gameVolume);

        continueButton = createButton("Continue");
        mainMenuButton = createButton("Main Menu");

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        table.add(titleLabel).padBottom(50f).row();
        table.add(musicLabel).padBottom(10f).row();
        table.add(musicSlider).width(300).padBottom(40f).row();
        table.add(volumeLabel).padBottom(10f).row();
        table.add(volumeSlider).width(300).padBottom(40f).row();
        table.add(continueButton).width(300).height(60).padBottom(20f).row();
        table.add(mainMenuButton).width(300).height(60).padBottom(20f).row();

        addListeners();
    }

    private TextButton createButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.getLabel().setFontScale(1.3f);
        button.setColor(new Color(0.0f, 0.95f, 0.95f, 1f)); // turquoise color
        return button;
    }

    private void addListeners() {
        Color normalColor = new Color(0.0f, 0.95f, 0.95f, 1f);
        Color clickColor = new Color(0.4f, 1f, 1f, 1f);
        // Continue button returns to same paused game
        continueButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                audioManager.setMusicVolume();
                audioManager.playMusic();
                playScreen.resume();
                game.setScreen(playScreen);
                dispose();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                continueButton.setColor(clickColor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                continueButton.setColor(normalColor);
            }
        });
        // Main menu button resets the game
        mainMenuButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {;
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
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.musicVolume = musicSlider.getValue();
            }

        });
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.gameVolume = volumeSlider.getValue();
            }

        });


    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        stage.getViewport().update(width, height, true);
    }

    @Override public void show() {

    }
    @Override public void hide() {

    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void dispose() {
        stage.dispose();

    }
}
