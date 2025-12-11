package io.github.team6ENG.EscapeUni;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;



public class LeaderboardScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage;
    private final Skin skin;

    public LeaderboardScreen(Main game, Runnable onBack) {
        this.game = game;
        this.skin = game.buttonSkin;
        this.stage = new Stage((Viewport) game.viewport);
        Gdx.input.setInputProcessor(stage);

        buildUI(onBack);
    }

    private void buildUI(Runnable onBack) {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("Leaderboard", skin);
        title.setFontScale(2f);
        root.add(title).colspan(3).padBottom(20);
        root.row();

        root.add(new Label("#", skin)).pad(6);
        root.add(new Label("Name", skin)).pad(6).expandX().left();
        root.add(new Label("Score", skin)).pad(6);
        root.row();

        com.badlogic.gdx.utils.Array<ScoreEntry> entries = LeaderboardManager.getInstance().getTop(5);
        for (int i = 0; i < entries.size; i++) {
            ScoreEntry e = entries.get(i);
            root.add(new Label(String.valueOf(i + 1), skin)).pad(4);
            root.add(new Label(e.name, skin)).pad(4).expandX().left();
            root.add(new Label(String.valueOf(e.score), skin)).pad(4);
            root.row();
        }

        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onBack != null) onBack.run();
            }
        });

        root.add(back).colspan(3).padTop(16);
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
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
