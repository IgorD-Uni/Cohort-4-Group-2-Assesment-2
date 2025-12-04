package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Represents character select screen
 */
public class CharacterSelectScreen implements Screen {


    private final Main game;
    private Stage stage;

    /**
     * Initialises scene
     * @param game instance of Main
     */
    public CharacterSelectScreen(final Main game) {
        this.game = game;

        stage = new Stage(game.viewport, game.batch);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        table.bottom();
        table.defaults().pad(50).fillX().uniformX().padBottom(0);


        Texture img1 = new Texture(Gdx.files.internal("images/femaleSpriteImg.png"));
        TextureRegionDrawable drawable1 = new TextureRegionDrawable(img1);
        Texture img2 = new Texture(Gdx.files.internal("images/maleSpriteImg.png"));
        TextureRegionDrawable drawable2 = new TextureRegionDrawable(img2);
        drawable1.setMinSize(drawable1.getMinWidth() *10, drawable1.getMinHeight() * 10);
        drawable2.setMinSize(drawable2.getMinWidth() * 10, drawable2.getMinHeight() * 10);

        ImageButton characterButton1 = new ImageButton(drawable1);
        ImageButton characterButton2 = new ImageButton(drawable2);

        table.add(characterButton1).height(Value.percentHeight(1f, table));

        table.add(characterButton2).height(Value.percentHeight(1f, table));


        characterButton1.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.app.postRunnable(() -> {
                    game.activeSpritePath = "sprites/femaleSprite.png";
                    game.activeUniIDPath = "items/idFemale.png";
                    game.setScreen(new InstructionsScreen(game));
                    dispose();
                });
            } });
        characterButton2.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.app.postRunnable(() -> {
                    game.activeSpritePath = "sprites/maleSprite.png";
                    game.activeUniIDPath = "items/idMale.png";
                    game.setScreen(new InstructionsScreen(game));
                    dispose();
                });
            }
        });
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Render screen
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {

        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();


        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        String title = "Choose your player";
        GlyphLayout layout = new GlyphLayout(game.menuFont, title);
        float titleX = (worldWidth - layout.width) / 2;
        game.menuFont.setColor(Color.WHITE);
        game.menuFont.draw(game.batch, title, titleX, worldHeight * 0.9f);

        game.batch.end();

        stage.act();
        stage.draw();


    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {


        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }
}
