package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.ArrayList;

/**
 * Represents the interior of the Ron Cooke building.
 * Displays a black background with a welcome message.
 */
public class RonCookeScreen implements Screen {

    private final Main game;
    private final BuildingManager buildingManager;
    private final GameScreen gameScreen;  // keep reference to return
    private final BitmapFont font;
    private final BitmapFont smallFont;
    private Player player;
    private Image receptionist;
    private float stateTime;

    float worldWidth;
    float worldHeight ;

    float speechTimer = 0;
    ArrayList<String> speech = new ArrayList<String>();

    private boolean isEPressed = false;
    private boolean isPaused = false;
    public RonCookeScreen(Main game, BuildingManager buildingManager, GameScreen gameScreen) {
        this.game = game;
        this.buildingManager = buildingManager;
        this.gameScreen = gameScreen;
        this.font = game.menuFont;
        this.smallFont = game.gameFont;

        initialisePlayer((int) game.viewport.getWorldWidth()/2,(int) game.viewport.getWorldHeight()/2);
        initialiseReceptionist();
        stateTime = 0;
        speech.add("Locked yourself out huh?");
        speech.add("Take this key card");
        speech.add("     Wait");
        speech.add("It's gotten quite dark out there");
        speech.add("Take this torch.");
    }

    /**
     * Initialise player and set its position
     */
    private void initialisePlayer(int x, int y) {
        player = new Player(game, buildingManager.audioManager, gameScreen.mapLangwithBarriersId, gameScreen.mapWaterId);
        player.loadSprite(new TiledMapTileLayer( 400, 225, 16,16), 0, 16);
        player.sprite.setPosition(x, y);
        player.sprite.setScale(4);
        player.speed  = 2;

    }
    private void initialiseReceptionist(){
        Texture tex = new Texture(Gdx.files.internal("sprites/receptionist.png"));
        receptionist = new Image(tex);
        receptionist.setPosition((float) (game.viewport.getWorldWidth() * 0.2),  (game.viewport.getWorldHeight() - receptionist.getHeight()) /2.5f);
        receptionist.setScale(5);
    }

    @Override
    public void render(float delta) {
        // Clear to black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(!isPaused) {
            player.handleInput(delta, gameScreen.playerSpeedModifier);
            player.updatePlayer(stateTime);
        }
        game.batch.begin();

        player.sprite.draw(game.batch);
        receptionist.draw(game.batch,1);

        worldWidth = game.viewport.getWorldWidth();
        worldHeight = game.viewport.getWorldHeight();


        for(String key: gameScreen.items.keySet()){
            Collectable item = gameScreen.items.get(key);
            if(item.isVisible && !item.playerHas && item.originScreen.equals("RonCookeScreen")){
                item.img.draw(game.batch, 1);
                if (item.checkInRange(player.sprite.getX()- (player.sprite.getHeight()/2) , player.sprite.getY() - (player.sprite.getHeight()/2)) && isEPressed){
                    item.Collect();
                    isEPressed = false;

                }
            }
        }
        game.batch.end();
        renderUI();
        if(!isPaused) {
            game.gameTimer -= delta;
            if(game.gameTimer < 0){
                gameScreen.gameOver();
                return;
            }
            speechTimer += delta;
            if (gameScreen.items.get("keyCard").playerHas && gameScreen.items.get("torch").playerHas) {
                buildingManager.update(delta);
            }
            stateTime += delta;
            isEPressed = Gdx.input.isKeyJustPressed(Input.Keys.E);

            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                isPaused = true;
                game.setScreen(new PauseScreen(game, this, buildingManager.audioManager));

            }
        }
    }

    /**
     * Helper method: text rendering logic to avoid repeated setColor() calls
     * @param font  The BitmapFont to use for rendering
     * @param text  The text string to display
     * @param colour The colour of the text
     * @param x     The x-coordinate for text position
     * @param y     The y-coordinate for text position
     */
    private void drawText(BitmapFont font, String text, Color colour, float x, float y) {
        font.setColor(colour);
        font.draw(game.batch, text, x, y);
    }
    private GlyphLayout layout = new GlyphLayout();
    private void renderUI(){

        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();
        float itemXPos = (worldWidth - (gameScreen.numOfInventoryItems * 32))/2;
        String instructions = "";

        // draw items either in inventory or on screen
        for(String key:gameScreen.items.keySet()) {

            Collectable item = gameScreen.items.get(key);
            if (item.playerHas){
                item.img.setPosition(itemXPos, worldHeight * 0.8f);
                item.img.draw(game.batch, 1);
                itemXPos += 32;
            }
            else if (item.originScreen.equals("RonCookeScreen") && item.isVisible && item.checkInRange(player.sprite.getX()- 32, player.sprite.getY() - (player.sprite.getHeight()/2))){
                if (instructions.isEmpty()) {
                    instructions = "Press 'e' to collect " + key;
                }
            }
        }
        float y = worldHeight - 20f;
        float lineSpacing = 15f;

        // Requirements: Events tracker and game timer
        drawText(smallFont, ("Negative Events: " + game.foundNegativeEvents +"/" + game.totalNegativeEvents), Color.WHITE, 20, y);
        y -= lineSpacing;
        drawText(smallFont, ("Positive Events: "+ game.foundPositiveEvents+"/"+ game.totalPositiveEvents), Color.WHITE, 20, y);
        y -= lineSpacing;
        drawText(smallFont, ("Hidden Events:   "+ game.foundHiddenEvents+"/"+ game.totalHiddenEvents), Color.WHITE, 20, y);
        y -= lineSpacing;
        layout.setText(game.menuFont, instructions);
        float textX = (worldWidth - layout.width) / 2;
        drawText(font, instructions, Color.WHITE, textX, worldHeight * 0.75f);
        //Display time with 2 digits for seconds
        drawText(font, ((int)game.gameTimer/60 + ":" +((int)game.gameTimer % 60 <10?"0" :"" ) +(int)game.gameTimer % 60), Color.WHITE, worldWidth - 80f, worldHeight-20f);
        layout = new GlyphLayout(game.menuFont, ("Score: " + (int)game.score));
        drawText(font, ("Score: " +(int)game.score), Color.WHITE, (worldWidth - layout.width)/2, worldHeight-20f);

        if(gameScreen.items.get("keyCard").playerHas){
            speech.clear();
        }

        //If not yet received card
        if(!speech.isEmpty()){

            font.draw(game.batch, speech.get(0), worldWidth * 0.1f , worldHeight * 0.8f);

            if (speechTimer >= 3){
                speechTimer = 0;
                speech.remove(0);
            }
        } //else second time vising Ron Cooke
        else{
            gameScreen.items.get("keyCard").isVisible = true;
            gameScreen.items.get("keyCard").x = (float) (game.viewport.getWorldWidth() * 0.3);
            gameScreen.items.get("keyCard").y = (float) (game.viewport.getWorldHeight() *0.4);

            gameScreen.items.get("torch").isVisible = true;
            gameScreen.items.get("torch").x = (float) (game.viewport.getWorldWidth() * 0.3);
            gameScreen.items.get("torch").y = (float) (game.viewport.getWorldHeight() *0.45);

            if(gameScreen.items.get("keyCard").playerHas) {
                font.setColor(Color.GRAY);
                String exitText = "Press G to leave";
                GlyphLayout exitLayout = new GlyphLayout(font, exitText);
                font.draw(game.batch, exitText, (worldWidth - exitLayout.width) / 2, worldHeight - 50);
            }
        }

        game.batch.end();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { game.viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {
        isPaused = false;
    }
    @Override public void hide() {}
    @Override public void dispose() {

    }
}
