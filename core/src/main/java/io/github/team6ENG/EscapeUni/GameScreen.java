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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

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

    private OrthographicCamera camera;
    private TiledMapTileLayer collisionLayer;

    private boolean isPaused = false;
    private boolean isEPressed = false;
    OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap map;
    private Image mapImg;
    private final int mapWallsId = 1;
    public final int mapWaterId = 2;
    public final int mapLangwithBarriersId = 3;
    private final int tileDimensions  = 8;

    // Original goose and attack geese events
    Goose goose = new Goose();
    Goose attackGoose1 = new Goose();
    float stateTime;
    private Rectangle stealTorchTrigger;
    //Secret event entrance

    private Texture duckStatue = new Texture("images/Duck-statue.png");


    // Beer event
    private boolean beerActive = false;
    private float beerTimer = 0f;
    private boolean isScreenFlipped = false;
    private float beerMessageTimer = 0f;

    // Rotten pizza effect
    private boolean foodPoisoned = false;
    private float foodPoisonTimer = 0f;
    private float foodPoisonMessageTimer = 0f;
    private Texture whitePixel; // for the green overlay

    // Bell Event
    private boolean bellActive = false;
    private float bellDuration = 10f; // seconds the students stay active
    private float bellTimer = 0f;

    private Array<Student> bellStudents = new Array<>(); // Array of students
    private Texture studentTexture;

    // Student bell event UI
    private float bellMessageTimer = 0f;
    private boolean bellEventCounted = false;

    private boolean isCamOnGoose = false;
    private boolean hasGooseFood = false;
    private boolean gameoverTrigger = false;
    private boolean gooseStolenTorch = false;
    private boolean gooseHasHit = false;

    private final float probabilityOfHonk = 1000;
    private boolean hasShield = false;
    private boolean wasHit = false;
    private boolean airhornOn = false;
    private float airhornOnTimer = 0f;

    public final HashMap<String, Collectable> items = new HashMap<String, Collectable>();
    public int numOfInventoryItems = 0;

    private Texture busTexture;
    private float busX, busY;
    private boolean busVisible = false;
    private boolean playerOnBus = false;
    private boolean busLeaving = false;

    public float playerSpeedModifier = 1;

    public AudioManager audioManager;
    //Health system
    private HealthSystem healthSystem;
    private ShapeRenderer shapeRenderer;

    //Achievements
    public Achievements achievements;


    /**
     * Initialise the game elements
     * @param game - Instance of Main
     */
    public GameScreen(final Main game) {
        this.game = game;

        initialiseMap(0);

        initialiseAudio();

        initialisePlayer(940,1215); //Starting position x:940 , y:1215

        initialiseCamera();

        // Assesment 2 (remove lighting function)

        initialiseGoose(100,100);

        initialiseItems();

        initialiseBus();

        initialiseHealth();

        buildingManager = new BuildingManager(game, this, player, audioManager);

        achievements = game.achievements;

        stateTime = 0f;

        game.totalGameTime = game.gameTimer; // save starting timer
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
    //Initialise attack goose, differs in behaviour to original goose
    private void initialiseAttackGoose(Goose goose, float x, float y) {
        goose.loadSprite(collisionLayer, mapWallsId, tileDimensions);
        goose.x = x;
        goose.y = y;
        goose.isAttackGoose = true;
        goose.isRunningAway = false;
        goose.attackTimer = 15.0f;
    }

    /**
     * Load collectable items into items class
     * They will then appear on screen and allow the player to pick them up
     */
    private void initialiseItems() {
        int playerX = 940;
        int playerY = 1215;
        items.put("gooseFood", new Collectable(game, "items/gooseFood.png",   400, 1475, 0.03f, true, "GameScreen", audioManager));
        items.put("keyCard", new Collectable(game, game.activeUniIDPath,   300, 200, 0.05f, false, "RonCookeScreen", audioManager));
        items.put("torch", new Collectable(game, "items/torch.png",   300, 220, 0.1f, false, "RonCookeScreen", audioManager));
        items.put("pizza", new Collectable(game, "items/pizza.png", 600, 100, 0.4f, true, "LangwithScreen", audioManager));
        items.put("phone", new Collectable(game, "items/phone.png", 100, 100, 0.05f, true, "LangwithScreen", audioManager));
        items.put("shield", new Collectable(game, "items/shield.png", 960, 1150, 0.2f, true, "GameScreen", audioManager));
        items.put("healthBoost", new Collectable(game, "items/healthBoost.png", 900, 1200, 0.2f, true, "GameScreen", audioManager));
        items.put("healthBoost2", new Collectable(game, "items/healthBoost.png", 520, 1500, 0.2f, true, "GameScreen", audioManager));
        items.put("beer", new Collectable(game, "items/beerIcon.png", 480, 800, 0.1f, true, "GameScreen", audioManager));
        items.put("rottenPizza", new Collectable(game, "items/rottenPizzaIcon.png", 600, 1250, 0.1f, true, "GameScreen", audioManager));
        items.put("bell", new Collectable(game,"items/bell.png",480,600,0.03f,true,"GameScreen", audioManager));
        items.put("airhorn", new Collectable(game, "items/airhorn.png", 350, 100, 0.2f, true, "GameScreen", audioManager));
        items.put("homework", new Collectable(game, "items/homework.png", 100, 200, 0.2f, true, "GameScreen", audioManager));
    }
    private void initialiseBus() {
        busTexture = new Texture(Gdx.files.internal("images/bus.png"));
        busX = 1100;
        busY = 1545;
    }
    private  void initialiseAudio() {
        audioManager = new AudioManager(game);

    }
    private void initialiseHealth() {
        healthSystem = new HealthSystem();
        shapeRenderer = new ShapeRenderer();
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

            player.sprite.setX(busX);
            player.sprite.setY(busY);
            Gdx.gl.glClearColor(0, 0, 0, Math.min(1, (busX - 1500) / 300f));

            if (busX < 950 && !gameoverTrigger) {
                final int finalScore = Math.max(0, (int) game.score);
                winGame(finalScore);
            }

        }

        if(!isPaused && !busLeaving) {
            updateCamera();

            game.gameTimer -= delta;
            game.score -= delta;
            handleInput(delta);
            player.handleInput(delta, playerSpeedModifier);

            // Update negative event timers
            if (beerActive) {
                beerTimer -= delta;
                beerMessageTimer -= delta;
                if (beerTimer <= 0) {
                    beerActive = false;
                    isScreenFlipped = false; // reset screen flip
                    items.remove("bell");
                    numOfInventoryItems -= 1;
                }
            }
            if (airhornOn) {
                airhornOnTimer -= delta;
                if (airhornOnTimer <= 0) {
                    airhornOn = false;
                }
            }


            if (foodPoisoned) {
                foodPoisonTimer -= delta;
                foodPoisonMessageTimer -= delta;

                playerSpeedModifier = 0.5f;

                if (foodPoisonTimer <= 0f) {
                    foodPoisoned = false;
                    playerSpeedModifier = 1f;
                    items.remove("bell");
                    numOfInventoryItems -= 1;
                }
            }

            // Student crowd event
            if (bellActive) {
                bellTimer -= delta;

                if (bellMessageTimer > 0f) {
                    bellMessageTimer -= delta;
                }

                // Update students
                for (Student s : bellStudents) {
                    s.update(delta);

                    // Check collision with player
                    Rectangle playerBounds = new Rectangle(
                            player.sprite.getX(), player.sprite.getY(),
                            player.sprite.getWidth(), player.sprite.getHeight()
                    );

                    if (playerBounds.overlaps(s.getBounds())) {
                        playerSpeedModifier = 0.5f; // slow player on touch
                    }
                }

                if (bellTimer <= 0f) {
                    bellActive = false;
                    bellStudents.clear();
                    playerSpeedModifier = 1f; // reset speed
                    items.remove("bell");
                    numOfInventoryItems -= 1;
                }
            }


            float mapWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
            float mapHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();

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
                        player.isMoving, false, delta);
            }
            else{
                int[] runCoords = goose.nextRunLocation();
                goose.moveGoose(stateTime,runCoords[0],runCoords[1],true, false, delta);
                }
            // If there are baby geese, they follow the goose directly in front of them
            Goose trail = goose;
            float stateOffset = 0.075f;
            while (trail.baby != null) {
                trail.baby.moveGoose(stateTime - stateOffset,
                        trail.x,
                        trail.y,
                        player.isMoving, trail.isSleeping, delta);
                stateOffset += 0.075f;
                trail = trail.baby;
            }

            //Checking for achievements
            achievements.update(delta);

            // 1. Finding all hidden events
            if (achievements != null && game != null) {
                achievements.update(delta);

                try {
                    if (game.foundHiddenEvents == game.totalHiddenEvents) {
                        achievements.unlock("Hidden Master");
                    }
                } catch (Exception e) {
                    Gdx.app.error("Achievements", "Error unlocking achievement", e);
                }
            }

            // 2. Going Swimming
            if (player.hasEnteredWaterOnce && !achievements.isUnlocked("Going Swimming")) {
                audioManager.playWaterSplash();
                achievements.unlock("Going Swimming");
            }

            // 3. Speedy Finish (after game over)

            // Check if player can pick up items
            for(String key: items.keySet()){
                Collectable item = items.get(key);
                if(!item.playerHas && item.isVisible && item.originScreen.equals("GameScreen")){
                    if (item.checkInRange(player.sprite.getX(), player.sprite.getY()) && isEPressed){

                        // === NEGATIVE EVENTS ===
                        // "tipsy" event
                        if (key.equals("beer")) {
                            beerActive = true;
                            beerTimer = 15f; // 15 seconds
                            isScreenFlipped = true; // activate screen flip
                            beerMessageTimer = 4f; // show message for 4 seconds
                            item.Collect();
                            numOfInventoryItems += 1;
                            game.foundNegativeEvents += 1;
                        }

                        // "food poisoning" event
                        if (key.equals("rottenPizza")) {
                            foodPoisoned = true;
                            foodPoisonTimer = 15f; // 15 seconds
                            foodPoisonMessageTimer = 4f; // show message for 4 seconds
                            item.Collect();
                            numOfInventoryItems += 1;
                            game.foundNegativeEvents += 1;
                        }

                        // "student crowd" event
                        // STUDENT CROWD EVENT
                        if (key.equals("bell")) {bellActive = true;
                            bellActive = true;
                            bellTimer = 8f;
                            bellMessageTimer = 4f;   // show warning for 4 seconds
                            bellEventCounted = true;
                            item.Collect();
                            numOfInventoryItems += 1;
                            game.foundNegativeEvents += 1;
                            audioManager.playBellSound();
                            bellStudents.clear();

                            float baseX = player.sprite.getX();
                            float baseY = player.sprite.getY();

                            float spacing = 40f;
                            float speed = 90f;

                            // ===== TOP LANE (walk LEFT) =====
                            for (int i = 0; i < 8; i++) {
                                float x = baseX + 300 + i * spacing;   // spawn to the right
                                float y = baseY + 40;
                                bellStudents.add(new Student(x, y, speed, -1));
                            }

                            // ===== BOTTOM LANE (walk RIGHT) =====
                            for (int i = 0; i < 8; i++) {
                                float x = baseX - 300 - i * spacing;   // spawn to the left
                                float y = baseY - 40;
                                bellStudents.add(new Student(x, y, speed, +1));
                            }

                        }

                        // ======================


                        // === POSITIVE AND HIDDEN EVENTS ===
                        if (key.equals("gooseFood")){
                            hasGooseFood = true;
                            item.Collect();
                            numOfInventoryItems += 1;
                            game.foundHiddenEvents += 1;
                            game.foundPositiveEvents += 1;
                        }
                        if (key.startsWith("shield")){
                            healthSystem.shieldOn();
                            hasShield = true;
                            item.Collect();
                            numOfInventoryItems += 1;
                            game.foundPositiveEvents += 1;
                        }
                        if (key.startsWith("healthBoost")){
                            healthSystem.heal(20f);
                            item.isVisible = false;
                            item.playSound();
                            game.foundPositiveEvents += 1;
                        }
                        if (key.equals("airhorn")){
                            audioManager.playHonk();
                            game.score -= 30;
                            goose.pauseFor(15f);
                            if (attackGoose1 != null) {
                                attackGoose1.isRunningAway = true;
                            }
                            airhornOn = true;
                            airhornOnTimer = 0.5f;
                            item.isVisible = false;
                            item.playSound();
                            game.foundHiddenEvents += 1;
                        }
                        if (key.equals("homework")){
                            game.score += 50;
                            item.Collect();
                            numOfInventoryItems += 1;
                            initialiseAttackGoose(attackGoose1, 100, 450);
                            game.foundNegativeEvents += 1;
                        }
                        isEPressed = false;

                        // ======================

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
                numOfInventoryItems -= 1;
                goose.loadBabyGoose(0);
                game.foundHiddenEvents += 1;
                game.score += 100;
                hasGooseFood = false;
                //Stop attacking human after food fed
                goose.hadGooseFood = true;
            }

            isEPressed = false;


            //Health system update, and shield activation duration
            healthSystem.update(delta);

            if (hasShield && !healthSystem.isInvincible()) {
                hasShield = false;
                items.remove("shield");
                numOfInventoryItems -= 1;
            }

            Rectangle playerBounds = new Rectangle(
                player.sprite.getX(),
                player.sprite.getY(),
                player.sprite.getWidth(),
                player.sprite.getHeight()
            );

            goose.checkHitbox(playerBounds, healthSystem, goose.hadGooseFood, delta);
            if (!gooseHasHit && healthSystem.isHit()) {
                game.foundNegativeEvents += 1;
                gooseHasHit = true;
            }

            //Update attack geese movement and check hitbox
            if (attackGoose1.isAttackGoose && !attackGoose1.ranAway()) {
                attackGoose1.moveGoose(stateTime,
                        player.sprite.getX() + (player.sprite.getWidth() / 2),
                        player.sprite.getY() + (player.sprite.getHeight() / 2),
                        true, false, delta);
                attackGoose1.checkHitbox(playerBounds, healthSystem, false, delta);
            }

            if (healthSystem.isHit() && !wasHit) {
                audioManager.playImpact();
            }
            wasHit = healthSystem.isHit();

            // Keep sprites in map boundary
            player.sprite.setX(Math.max(0, Math.min(player.sprite.getX(), mapWidth - player.sprite.getWidth())));
            player.sprite.setY(Math.max(0, Math.min(player.sprite.getY(), mapHeight - player.sprite.getHeight())));
            goose.x = Math.max(0, Math.min(goose.x, mapWidth - goose.getWidth()));
            goose.y = Math.max(0, Math.min(goose.y, mapHeight - goose.getHeight()));

        } // End isPaused

        if (!gameoverTrigger) {
            if (healthSystem.isDead()) {
                loseGame(); // Dead = 0 score
                return;
            } else if (game.gameTimer <= 0) {
                loseGame(); // Time ran out
                return;
            }
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
    private void updateCamera() {

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
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        studentTexture = new Texture(Gdx.files.internal("sprites/student.png"));
        shapeRenderer = new ShapeRenderer();


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

        game.batch.setColor(Color.GRAY);
        if (attackGoose1.isAttackGoose && attackGoose1.currentGooseFrame != null) {
            game.batch.draw(attackGoose1.currentGooseFrame, attackGoose1.x, attackGoose1.y);
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
                    if (healthSystem.isHit()) { player.sprite.setColor(1, 0.5f, 0.5f, 1); }
                    player.sprite.draw(game.batch);
                    if (healthSystem.isHit()) { player.sprite.setColor(1, 1, 1, 1); }
                    game.batch.flush();
                    ScissorStack.popScissors();
                }
            }
            else{
                if (healthSystem.isHit()) { player.sprite.setColor(1, 0.5f, 0.5f, 1); }
                player.sprite.draw(game.batch);
                if (healthSystem.isHit()) { player.sprite.setColor(1, 1, 1, 1); }
            }

        }

        //Draw secret event entrance
        game.batch.draw(duckStatue, 1275,100, 200,200);

        game.batch.draw(busTexture, busX, busY, 100, 60);
        int mapWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        int mapHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();

        game.batch.end();

        // ---- TIPSY EVENT OVERLAY ----
        if (beerActive) {
            game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
            game.batch.begin();

            String drunkMessage = "Woaah - you're drunk!";
            GlyphLayout layout = new GlyphLayout(game.menuFont, drunkMessage);
            float baseX = (game.viewport.getWorldWidth() - layout.width) / 2f;
            float baseY = game.viewport.getWorldHeight() * 0.75f;
            float waveAmplitude = 5f;
            float time = stateTime * 5f;

            for (int i = 0; i < drunkMessage.length(); i++) {
                char c = drunkMessage.charAt(i);
                float offsetY = (float) Math.sin(time + i * 0.5f) * waveAmplitude;
                game.menuFont.draw(game.batch, String.valueOf(c), baseX + i * 14, baseY + offsetY);
            }

            game.batch.end();
        }


        // ---- FOOD POISONING OVERLAY ----
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        if (foodPoisoned) {
            game.batch.begin();
            float fadeOutDuration = 2f; // last 2 seconds fade out
            float alpha = 0.35f;

            if (foodPoisonTimer < fadeOutDuration) {
                alpha = 0.35f * (foodPoisonTimer / fadeOutDuration);
            }

            game.batch.setColor(0f, 0.4f, 0f, alpha);

            game.batch.draw(
                    whitePixel,
                    0, 0,
                    game.viewport.getWorldWidth(),
                    game.viewport.getWorldHeight()
            );
            game.batch.setColor(Color.WHITE);
            game.batch.end();
        }

        if (foodPoisonMessageTimer > 0f) {
            game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
            game.batch.begin();
            game.menuFont.draw(
                    game.batch,
                    "Oh no! You've got food poisoning!",
                    game.viewport.getWorldWidth() / 2f - 220,
                    game.viewport.getWorldHeight() * 0.75f

            );
            game.batch.end();


        }




        if (isScreenFlipped) {
            camera.up.set(0, -1, 0); // flip camera vertically
        } else {
            camera.up.set(0, 1, 0);  // normal
        }
        camera.update();

        // ---- STUDENT CROWD EVENT ----(Assesment 2)
        if (bellActive) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            game.batch.setColor(Color.WHITE);

            for (Student s : bellStudents) {
                game.batch.draw(studentTexture, s.x, s.y, s.width, s.height);
            }

            game.batch.end();

            if (bellMessageTimer > 0f) {
                game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
                game.batch.begin();

                // Grey overlay
                game.batch.setColor(0.3f, 0.3f, 0.3f, 0.35f);
                game.batch.draw(
                        whitePixel,
                        0, 0,
                        game.viewport.getWorldWidth(),
                        game.viewport.getWorldHeight()
                );
                game.batch.setColor(Color.WHITE);

                // Warning text
                String msg = "Oh no! You're being swarmed by students!\n"
                        + "Watch out or they'll slow you down!";

                GlyphLayout layout = new GlyphLayout(game.menuFont, msg);
                float x = (game.viewport.getWorldWidth() - layout.width) / 2f;
                float y = game.viewport.getWorldHeight() * 0.75f;

                game.menuFont.draw(game.batch, msg, x, y);

                game.batch.end();
            }
        }

        healthSystem.render(shapeRenderer, camera);
        game.batch.setColor(Color.WHITE);

        //Airhorn flash visually cues scaring the geese
        if (airhornOn) {
            game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
            game.batch.begin();
            float alpha = airhornOnTimer / 0.5f;
            game.batch.setColor(1f, 1f, 1f, alpha * 0.6f);
            game.batch.draw(whitePixel, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
            game.batch.setColor(Color.WHITE);
            game.batch.end();
        }

        renderUI();

        game.batch.begin();
            if (achievements != null) {
                achievements.render(game.batch, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
            }
        game.batch.end();



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
                String useInstructions = getInstructions(key);
                if (!useInstructions.isEmpty()) {
                    instructions = useInstructions;
                }

            }
            else if (item.originScreen.equals("GameScreen") && item.isVisible && item.checkInRange(player.sprite.getX(), player.sprite.getY())) {
                if (key.startsWith("healthBoost")) {
                    instructions = "Press 'e' to increase your health";
                }
                else if (key.startsWith("shield")) {
                    instructions = "Press 'e' to have immunity for 10 seconds";
                }
                else if (key.equals("airhorn")) {
                    instructions = "Press 'e' to scare off geese";
                }
                else {
                    instructions = "Press 'e' to collect " + key;
                }
            }
        }
        //Draw instructions
        GlyphLayout layout = new GlyphLayout(game.menuFont, instructions);
        float textX = (worldWidth - layout.width) / 2;

        drawText(bigFont, instructions, Color.BLACK, textX, worldHeight * 0.75f);

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

        // Game instructions
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

    public Player getPlayer() {
        return player;
    }

    //Losing game depending on losing health or time up (bus gone)
    public void loseGame() {
        if (gameoverTrigger) return;
        gameoverTrigger = true;

        audioManager.stopMusic();
        audioManager.stopFootsteps();

        Gdx.app.postRunnable(() -> {
            game.setScreen(new GameOverNamePrompt(
                    game,
                    0,
                    name -> {
                        LeaderboardManager.getInstance().addScore(name, 0);
                        game.setScreen(new GameOverScreen(
                                game,
                                "Sorry you missed the bus, better luck next time",
                                0
                        ));
                    }
            ));
        });
    }

    public void winGame(int finalScore) {
        if (gameoverTrigger) return;
        gameoverTrigger = true;

        audioManager.stopMusic();
        audioManager.stopFootsteps();

        // Speedy Finish achievement
        float elapsedTime = game.totalGameTime - game.gameTimer;
        if (elapsedTime <= 170f) {
            achievements.unlock("Sfpeedy Finish");
        }

        Gdx.app.postRunnable(() -> {
            game.setScreen(new GameOverNamePrompt(
                    game,
                    finalScore,
                    name -> {
                        LeaderboardManager.getInstance().addScore(name, finalScore);
                        game.setScreen(new LeaderboardScreen(
                                game,
                                () -> game.setScreen(new MainMenuScreen(game))
                        ));
                    }
            ));
        });
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

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        if (buildingManager != null) {
            buildingManager.dispose();
        }

        if (busTexture != null) busTexture.dispose();

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (whitePixel != null) whitePixel.dispose();
    }
}
