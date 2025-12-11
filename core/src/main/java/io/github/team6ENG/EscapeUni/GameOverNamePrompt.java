package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class GameOverNamePrompt extends ScreenAdapter {
    public interface NameSubmitCallback {
        void onSubmitted(String name);
    }

    private final Main game;
    private final Stage stage;
    private final Skin skin;
    private final int finalScore;
    private final NameSubmitCallback callback;

    public GameOverNamePrompt(Main game, int finalScore, NameSubmitCallback callback) {
        this.game = game;
        this.finalScore = finalScore;
        this.callback = callback;
        this.skin = game.buttonSkin;
        this.stage = new Stage((Viewport) game.viewport);
        Gdx.input.setInputProcessor(stage);
        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Label title = new Label("Enter your name", skin);
        title.setAlignment(Align.center);
        root.add(title).colspan(2).padBottom(20);
        root.row();

        final TextField nameField = new TextField("", skin);
        nameField.setMessageText("Your name...");
        nameField.setMaxLength(12);
        root.add(nameField).width(300).padBottom(10);
        root.row();

        TextButton submit = new TextButton("Submit", skin);
        TextButton cancel = new TextButton("Cancel", skin);

        root.add(submit).width(140).pad(8);
        root.add(cancel).width(140).pad(8);

        submit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) name = "Player";

                // call your callback
                callback.onSubmitted(name);
            }
        });


        cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.onSubmitted("Player");
            }
        });


        // Try to focus the field
        stage.setKeyboardFocus(nameField);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        // clear input processor if this stage owned it
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
