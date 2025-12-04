package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import java.util.HashMap;
import java.util.Random;

/**
 * GameScreen - main gameplay screen
 *
 */
public class GameScreen implements Screen {

    private static final boolean DEBUG = false;

    private final Main game;

    public Player player;
    private BuildingManager buildingManager;

    public OrthographicCamera camera;
    private TiledMapTileLayer collisionLayer;
    public Lighting lighting;
    public boolean isDark = false;

    private boolean isPaused = false;
    private boolean isEPressed = false;
    OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap map;
    private Image mapImg;
    private final int mapWallsId = 1;
    public final int mapWaterId = 2;
    public final int mapLangwithBarriersId = 3;
    private final int tileDimensions  = 8;

    Goose goose = new Goose();
    float stateTime;
    private Rectangle stealTorchTrigger;


    public boolean hasTorch = false;
    private boolean isTorchOn = false;
    private boolean isCamOnGoose = false;
    private boolean hasGooseFood = false;
    private boolean gameoverTrigger = false;
    private boolean gooseStolenTorch = false;

    private final float probabilityOfHonk = 1000;

    public final HashMap<String, Collectable> items = new HashMap<String, Collectable>();
    public int numOfInventoryItems = 0;

    private Texture busTexture;
    private float busX, busY;
    private boolean busVisible = false;
    private boolean playerOnBus = false;
    private boolean busLeaving = false;

    public float playerSpeedModifier = 1;

    public AudioManager audioManager;

    /**
     * Initialise the game elements
     * @param game - Instance of Main
     */
    public GameScreen(final Main game) {
        this.game = game;

        initialiseMap(0);

        initialiseAudio();

        initialisePlayer(940,1215);

        initialiseCamera();

        initialiseLighting();

        initialiseGoose(100,100);

        initialiseItems();

        initialiseBus();

        buildingManager = new BuildingManager(game, this, player, audioManager);
        stateTime = 0f;
    }

    /**
     * Load map and collision layer
     */
    private void initialiseMap(int wallLayer) {
        Texture mapTex = new Texture(Gdx.files.internal("tileMap/map.png"));
        mapImg = new Image(mapTex);
        map = new TmxMapLoader().load("tileMap/map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1);
        collisionLayer = (TiledMapTileLayer)map.getLayers().get(wallLayer);
    }

    /**
     * Initialise player and set its position
     */
    private void initialisePlayer(int x, int y) {
        player = new Player(game, audioManager, mapLangwithBarriersId, mapWaterId);
        player.loadSprite(collisionLayer, mapWallsId, tileDimensions);
        player.sprite.setPosition(x, y);
        player.speed = 1;

    }

    /**
     * Initialise camera and set to position of player
     */
    private void initialiseCamera() {
        camera = new OrthographicCamera(400,225);
        camera.position.set(
            player.sprite.getX() + player.sprite.getWidth() / 2,
            player.sprite.getY() + player.sprite.getHeight() / 2,
            0);
        camera.update();
    }

    /**
     * Initialise goose and set its position
     */
    private void initialiseGoose(int x, int y){

        goose.loadSprite(collisionLayer, mapWallsId, tileDimensions);
        goose.x = x;
        goose.y = y;

        stealTorchTrigger = new com.badlogic.gdx.math.Rectangle(510, 560, 50, 50);

    }


    /**
    * Add and hide light circles for player and goose
     */
    private void initialiseLighting() {
        int mapWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        int mapHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();

        lighting = new Lighting(mapWidth, mapHeight);

        lighting.addLightSource("playerTorch",
            player.sprite.getX() + (player.sprite.getWidth() / 2),
            player.sprite.getY() + (player.sprite.getHeight() / 2),
            new Color(0, 0, 0, 0),
            50);
        lighting.addLightSource("playerNoTorch",
            player.sprite.getX() + (player.sprite.getWidth() / 2),
            player.sprite.getY() + (player.sprite.getHeight() / 2),
            new Color(0, 0, 0, 0.4f),
            12);
        lighting.addLightSource("gooseTorch",goose.x+ (goose.getWidth() / 2), goose.y + (goose.getHeight() / 2), new Color(1,0.2f,0.1f,0.4f), 30);
        lighting.addLightSource("gooseNoTorch",goose.x+ (goose.getWidth() / 2), goose.y + (goose.getHeight() / 2), new Color(0, 0, 0, 0.4f), 10);

        lighting.isVisible("playerTorch", false);
        lighting.isVisible("playerNoTorch", false);
        lighting.isVisible("gooseTorch", false);
        lighting.isVisible("gooseNoTorch", false);


    }

    /**
     * Load collectable items into items class
     * They will then appear on screen and allow the player to pick them up
     */
    private void initialiseItems() {
        items.put("gooseFood", new Collectable(game, "items/gooseFood.png",   500, 1500, 0.03f, true, "GameScreen", audioManager));
        items.put("keyCard", new Collectable(game, game.activeUniIDPath,   300, 200, 0.05f, false, "RonCookeScreen", audioManager));
        items.put("torch", new Collectable(game, "items/torch.png",   300, 220, 0.1f, false, "RonCookeScreen", audioManager));
        items.put("pizza", new Collectable(game, "items/pizza.png", 600, 100, 0.4f, true, "LangwithScreen", audioManager));
        items.put("phone", new Collectable(game, "items/phone.png", 100, 100, 0.05f, true, "LangwithScreen", audioManager));


    }
    private void initialiseBus() {
        busTexture = new Texture(Gdx.files.internal("images/bus.png"));
        busX = 1100;
        busY = 1545;
    }
    private  void initialiseAudio() {
        audioManager = new AudioManager(game);

    }

    /**
     * Call every frame to update game state
     * @param delta - Time since last frame
     */
    private void update(float delta) {


        // bus logic
        if (items.get("phone").playerHas && !playerOnBus) {
            player.hasEnteredLangwith = true;
            float dx = player.sprite.getX() - busX;
            float dy = player.sprite.getY() - busY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 50f) {
                playerOnBus = true;
                isPaused = true;
                hasTorch = false;
                //player.sprite.setAlpha(0f);
                game.gameFont.setColor(Color.BLUE);
                game.gameFont.getData().setScale(2f);
            }
        }

        if (playerOnBus) {
            audioManager.stopFootsteps();
            game.musicVolume = 0;
            game.gameVolume = 0;
            audioManager.stopMusic();
            busLeaving = true;
            busX -= 80 * delta;
            lighting.isVisible("playerTorch", true);
            lighting.isVisible("playerNoTorch", false);

            player.sprite.setX(busX);
            player.sprite.setY(busY);
            Gdx.gl.glClearColor(0, 0, 0, Math.min(1, (busX - 1500) / 300f));

            if (busX < 950) {
                Gdx.app.postRunnable(() -> game.setScreen(
                    new WinScreen(game)
                ));
            }
        }
        lighting.updateLightSource("playerTorch", player.sprite.getX() + (player.sprite.getWidth() / 2), player.sprite.getY() + (player.sprite.getHeight() / 2));

        if(!isPaused && !busLeaving) {
            updateCamera();

            game.gameTimer -= delta;
            game.score -= delta;
            handleInput(delta);
            player.handleInput(delta, playerSpeedModifier);
            float mapWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
            float mapHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();




            // Update light positions
            lighting.updateLightSource("playerTorch", player.sprite.getX() + (player.sprite.getWidth() / 2), player.sprite.getY() + (player.sprite.getHeight() / 2));
            lighting.updateLightSource("playerNoTorch", player.sprite.getX() + (player.sprite.getWidth() / 2), player.sprite.getY() + (player.sprite.getHeight() / 2));
            lighting.updateLightSource("gooseNoTorch", goose.x+ (goose.getWidth() / 2), goose.y + (goose.getHeight() / 2));

            if(gooseStolenTorch){
                lighting.updateLightSource("gooseTorch", goose.x+ (goose.getWidth() / 2), goose.y + (goose.getHeight() / 2));

            }

            player.updatePlayer(stateTime);
            if(player.isMoving && !player.isFootsteps){
                audioManager.loopFootsteps();
                player.isFootsteps = true;
            }
            else if (!player.isMoving){
                player.isFootsteps = false;
                audioManager.stopFootsteps();
            }

            // Goose follow player
            if(!gooseStolenTorch) {
                goose.moveGoose(stateTime,
                    player.sprite.getX() + (player.sprite.getWidth() / 2) - 20,
                    player.sprite.getY() + (player.sprite.getHeight() / 2),
                    player.isMoving, false);
            }
            else{
                int[] runCoords = goose.nextRunLocation();
                goose.moveGoose(stateTime,runCoords[0],runCoords[1],true, false);
                }
            // If there are baby geese, they follow the goose directly in front of them
            Goose trail = goose;
            float stateOffset = 0.075f;
            while (trail.baby != null) {
                trail.baby.moveGoose(stateTime - stateOffset,
                    trail.x,
                    trail.y,
                    player.isMoving, trail.isSleeping);
                stateOffset += 0.075f;
                trail = trail.baby;
            }

            // Check if player can pick up items
            for(String key: items.keySet()){
                Collectable item = items.get(key);
                if(!item.playerHas && item.isVisible && item.originScreen.equals("GameScreen")){
                    if (item.checkInRange(player.sprite.getX(), player.sprite.getY()) && isEPressed){
                        if (key == "statue"){
                            item.isVisible = false;
                            isEPressed = false;
                            continue;
                        }
                        item.Collect();
                        numOfInventoryItems += 1;
                        isEPressed = false;
                        if (key.equals("gooseFood")){
                            hasGooseFood = true;
                        }

                    }
                }
            }

            // Feed goose if player has food and in range
            float dx = (goose.x + (goose.getWidth())/2) - (player.sprite.getX()+ (player.sprite.getWidth()/2));
            float dy = (goose.y + (goose.getHeight()/2)) - (player.sprite.getY() + (player.sprite.getHeight()/2));

            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < 30f && hasGooseFood && isEPressed) {
                items.get("gooseFood").playSound();
                items.remove("gooseFood");
                goose.loadBabyGoose(0);
                game.foundHiddenEvents += 1;
                game.score += 100;
                hasGooseFood = false;
            }

            isEPressed = false;


            if(hasTorch || gooseStolenTorch){
                Rectangle playerRect = new Rectangle(
                    player.sprite.getX(),
                    player.sprite.getY(),
                    player.sprite.getWidth(),
                    player.sprite.getHeight()
                );

                if(playerRect.overlaps(stealTorchTrigger) && !goose.attackModeActivated){
                    goose.attackMode();
                    audioManager.playHonk();

                }
                else if(goose.attackModeActivated){
                    Rectangle gooseRect = new Rectangle( goose.x,goose.y,goose.getWidth(),goose.getHeight());
                    if(playerRect.overlaps(gooseRect) && !gooseStolenTorch){
                        gooseStolenTorch = true;
                        isCamOnGoose = true;
                        hasTorch = false;
                        game.foundNegativeEvents += 1;
                        lighting.isVisible("gooseTorch", true);
                        lighting.isVisible("gooseNoTorch", false);
                        lighting.isVisible("playerTorch", false);
                        lighting.isVisible("playerNoTorch", true);
                        audioManager.playHonk();

                    }
                    else if(!playerRect.overlaps(stealTorchTrigger) &&playerRect.overlaps(gooseRect) && gooseStolenTorch){

                        isCamOnGoose = false;
                        lighting.isVisible("gooseTorch", false);
                        lighting.isVisible("gooseNoTorch", true);
                        lighting.isVisible("playerTorch", true);
                        lighting.isVisible("playerNoTorch", false);


                        if(!goose.isSleeping && !hasTorch){
                            game.score += 100;
                        }

                        hasTorch = true;
                    }
                }

            }

            // Keep sprites in map boundary
            player.sprite.setX(Math.max(0, Math.min(player.sprite.getX(), mapWidth - player.sprite.getWidth())));
            player.sprite.setY(Math.max(0, Math.min(player.sprite.getY(), mapHeight - player.sprite.getHeight())));
            goose.x = Math.max(0, Math.min(goose.x, mapWidth - goose.getWidth()));
            goose.y = Math.max(0, Math.min(goose.y, mapHeight - goose.getHeight()));

        } // End isPaused

        // If time up
        if(!gameoverTrigger && game.gameTimer <= 0) {
           gameOver();
           return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                audioManager.pauseMusic();
            audioManager.stopFootsteps();
            game.setScreen(new PauseScreen(game, GameScreen.this, audioManager));
        }

        buildingManager.update(delta);

        playAudio();

    }

    /**
     * Make camera follow player
     */
     void updateCamera() {

        float mapWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        float mapHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();

        float playerCenterX = player.sprite.getX() + player.sprite.getWidth() / 2f;
        float playerCenterY = player.sprite.getY() + player.sprite.getHeight() / 2f;

        float gooseCenterX = goose.x + goose.getWidth() / 2f;
        float gooseCenterY = goose.y + goose.getHeight() / 2f;

        float finalX = (playerCenterX + gooseCenterX) / 2f;
        float finalY = (playerCenterY + gooseCenterY) / 2f;

        // camera follows player
        float slope = 0.1f;

        if(isCamOnGoose && Math.abs(finalX-playerCenterX)<100 && Math.abs(finalY-playerCenterY)<100){

            camera.position.x += (finalX - camera.position.x) * slope;
            camera.position.y += (finalY - camera.position.y) * slope;
        }
        else {
            camera.position.x += (playerCenterX - camera.position.x) * slope;
            camera.position.y += (playerCenterY - camera.position.y) * slope;
        }


        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        if (mapWidth > camera.viewportWidth) {
            camera.position.x = Math.max(halfWidth, Math.min(camera.position.x, mapWidth - halfWidth));
        } else {

            camera.position.x = mapWidth / 2f;
        }

        if (mapHeight > camera.viewportHeight) {
            camera.position.y = Math.max(halfHeight, Math.min(camera.position.y, mapHeight - halfHeight));
        } else {
            camera.position.y = mapHeight / 2f;
        }

        if (busLeaving) {
            camera.position.x = busX;
            camera.position.y = busY + 20;
            camera.update();
            return;
        }

        camera.update();
}


    @Override
    public void show() {
    }

    /**
     * Calls every frame to draw game screen
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (mapRenderer != null) {
            game.viewport.apply();
            mapRenderer.setView(camera);
            mapRenderer.render();
            Gdx.gl.glFlush();
        }
        buildingManager.renderBuildingMap(camera);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        mapImg.draw(game.batch, 1);
        stateTime += delta;

        game.batch.draw(goose.currentGooseFrame, goose.x, goose.y);

        //Draw yellow baby geese
        game.batch.setColor(Color.YELLOW);
        Goose trail = goose;
        while (trail.baby != null){
            trail = trail.baby;
            if (trail.currentGooseFrame != null) {

                game.batch.draw(trail.currentGooseFrame, trail.x, trail.y, 16f, 16f);

            }

        }
        game.batch.setColor(Color.WHITE);

        // Draw uncollected items in game
        for(String key: items.keySet()){
            Collectable item = items.get(key);
            if(item.isVisible && !item.playerHas && item.originScreen.equals( "GameScreen")){
                item.img.draw(game.batch, 1);
            }
        }
        if (player.sprite.getTexture() != null) {
            if(player.inWater) {
                Rectangle scissors = new Rectangle();
                Rectangle clipBounds = new Rectangle(player.sprite.getX(), player.sprite.getY() + (player.sprite.getHeight() * 0.4f), player.sprite.getWidth(), player.sprite.getHeight());
                ScissorStack.calculateScissors(camera, game.batch.getTransformMatrix(), clipBounds, scissors);
                if (ScissorStack.pushScissors(scissors)) {
                    player.sprite.draw(game.batch);
                    game.batch.flush();
                    ScissorStack.popScissors();
                }
            }
            else{
                player.sprite.draw(game.batch);
            }

        }
        if(hasTorch){
            player.torch.draw(game.batch, 1);
        }

        game.batch.draw(busTexture, busX, busY, 100, 60);
        int mapWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        int mapHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();

        if(isDark) {
            game.batch.draw(lighting.render(camera, mapWidth, mapHeight), 0, 0);
        }



        game.batch.end();

        renderUI();


    }
    Random random = new Random();
    private void playAudio(){
        int doHonk = random.nextInt((int) probabilityOfHonk);
        if(doHonk == 0 && !isPaused) {
            audioManager.playHonk();
        }
    }

    /**
     * Check for keyboard input
     * @param delta time in seconds since last frame
     */
    private void handleInput(float delta) {


        if(Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            isEPressed = true;
        }


        // Toggle the torch with click
        if(Gdx.input.justTouched() && hasTorch){
            isTorchOn = !isTorchOn;
            lighting.isVisible("playerTorch", isTorchOn);
            audioManager.playTorch();
        }

    }

    /**
     * Draw UI on screen
     */
    private void renderUI() {
        if (busLeaving) return;

        BitmapFont smallFont = game.gameFont;
        BitmapFont bigFont = game.menuFont;
        float worldHeight = game.viewport.getWorldHeight();
        float worldWidth = game.viewport.getWorldWidth();

        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        // Draw collected items in inventory bar
        // Display instructions if an item can be used or collected
        float itemXPos = (worldWidth - ((numOfInventoryItems) * 32))/2;
        String instructions= "";
        for(String key:items.keySet()) {
            Collectable  item = items.get(key);
            if (item.playerHas){
                item.img.setPosition(itemXPos, worldHeight * 0.8f);
                item.img.draw(game.batch, 1);
                itemXPos += 32;
                instructions = getInstructions(key);

            }
            else if (item.originScreen.equals("GameScreen") && item.isVisible &&
                key == "statue" && item.checkInRange(player.sprite.getX(), player.sprite.getY())){
                instructions = "Pray to the Long Boi";

            }
            else if (item.originScreen.equals("GameScreen") && item.isVisible && item.checkInRange(player.sprite.getX(), player.sprite.getY())) {
                instructions = "Press 'e' to collect " + key;

            }
        }
        //Draw instructions
        GlyphLayout layout = new GlyphLayout(game.menuFont, instructions);
        float textX = (worldWidth - layout.width) / 2;
        if(isDark){
            drawText(bigFont, instructions,Color.WHITE, textX, worldHeight* 0.75f);

        }else {
            drawText(bigFont, instructions, Color.BLACK, textX, worldHeight * 0.75f);
        }

        float y = worldHeight - 20f;
        float lineSpacing = 15f;

        // Requirements: Events tracker and game timer
        drawText(smallFont, ("Negative Events: " + game.foundNegativeEvents +"/" + game.totalNegativeEvents), Color.WHITE, 20, y);
        y -= lineSpacing;
        drawText(smallFont, ("Positive Events: "+ game.foundPositiveEvents +"/"+ game.totalPositiveEvents), Color.WHITE, 20, y);
        y -= lineSpacing;
        drawText(smallFont, ("Hidden Events:   "+ game.foundHiddenEvents+"/"+ game.totalHiddenEvents), Color.WHITE, 20, y);
        y -= lineSpacing;
        //Display time with 2 digits for seconds
        drawText(bigFont, ((int)game.gameTimer/60 + ":" +((int)game.gameTimer % 60 <10?"0" :"" ) +(int)game.gameTimer % 60), Color.WHITE, worldWidth - 80f, worldHeight-20f);
        layout = new GlyphLayout(game.menuFont, ("Score: " + (int)game.score));
        drawText(bigFont, ("Score: " +(int)game.score), Color.WHITE, (worldWidth - layout.width)/2, worldHeight-20f);


        if(gooseStolenTorch && !hasTorch){
            layout = new GlyphLayout(game.menuFont, "THE GOOSE STOLE YOUR TORCH\nCATCH IT QUICK!!!");
            drawText(bigFont, "THE GOOSE STOLE YOUR TORCH\nCATCH IT QUICK!!!", Color.RED, (worldWidth -layout.width)/2, worldHeight-80f);
        }
        // Game instructions
        if(hasTorch) {
            drawText(bigFont, "Left click to switch on torch", Color.ORANGE, 20, 80);
        }
        drawText(bigFont, "Press 'p' to pause", Color.WHITE, 20, 55);
        drawText(bigFont, "Use Arrow Keys or WASD to move", Color.WHITE, 20, 30);

        if(isPaused) {
            smallFont.draw(game.batch, "PAUSED", (float) worldWidth / 2, worldHeight - 100);
        }


        buildingManager.renderUI(game.batch, smallFont, bigFont, worldWidth, worldHeight);
        game.batch.end();

    }

    /**
     * Checks if inventory item can be used
     * @param key item being checked
     * @return String of instructions to display
     */
    private String getInstructions(String key) {
        if(key.equals("gooseFood")) {

            float dx = (goose.x + (goose.getWidth())/2) - (player.sprite.getX()+ (player.sprite.getWidth()/2));
            float dy = (goose.y + (goose.getHeight()/2)) - (player.sprite.getY() + (player.sprite.getHeight()/2));

            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < 30f) {
                return "Press 'e' to feed seeds to goose";
            }
        }
        return "";
    }

    public void gameOver(){
        gameoverTrigger = true;
        audioManager.stopMusic();
        audioManager.stopFootsteps();
        Gdx.app.postRunnable(() -> game.setScreen(
            new GameOverScreen(game, "Sorry you missed the bus, better luck next time")
        ));

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


    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

        isPaused = false;
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        // release texture memory
        if (player.sprite.getTexture() != null) {
            player.sprite.getTexture().dispose();
        }

        if (goose != null && goose.currentGooseFrame != null) {
            goose.currentGooseFrame.getTexture().dispose();
        }

        if (map != null) {
            map.dispose();
        }
        if (lighting != null) {
            lighting.dispose();
        }


        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        if (buildingManager != null) {
            buildingManager.dispose();
        }

        if (busTexture != null) busTexture.dispose();

    }
}
