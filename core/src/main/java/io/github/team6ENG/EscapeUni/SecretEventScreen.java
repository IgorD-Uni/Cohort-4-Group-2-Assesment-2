package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import org.w3c.dom.Text;


// Adding secret event - Long Boi dungeon  (Assesment 2)
public class SecretEventScreen implements Screen {

    private Texture map;
    private Texture LongBoi;

    private final Main game;
    private final BuildingManager buildingManager;
    private final GameScreen gameScreen;  // keep reference to return


    private Player player;
    private float stateTime;

    float worldWidth;
    float worldHeight;

    private float pauseTimer;

    private OrthographicCamera camera;
    private boolean isPaused = false;
    private boolean previous =false;
    private boolean didImpact =false;
    private boolean didQuack =false;

    private boolean didThunder =false;

    private Texture textIMG = new Texture("images/TEXTspecial.png");

    /**
     * Creates the secret dungeon
     *
     * @param game The main framework the game will be set  in
     * @param buildingManager connect the secret dungeon to the main map and to other rooms.
     * @param gameScreen connects to the main screen functionality and variables
     */

    public SecretEventScreen(Main game, BuildingManager buildingManager, GameScreen gameScreen) {
        this.game = game;
        this.buildingManager = buildingManager;
        this.gameScreen = gameScreen;
        initialisePlayer((int) 60, (int) game.viewport.getWorldHeight() / 2);
        stateTime = 0;

        map = new Texture("tileMap/dungeon.png");
        initialiseAudio();
        initialiseCamera();

        LongBoi = new Texture("sprites/LongBoi.png");
        pauseTimer = 0;
    }

    private void initialisePlayer(int x, int y) {
        player = new Player(game, buildingManager.audioManager, gameScreen.mapLangwithBarriersId, gameScreen.mapWaterId);
        player.loadSprite(new TiledMapTileLayer(400, 225, 16, 16), 0, 16);
        player.sprite.setPosition(x, y);
        player.sprite.setScale(4);
        player.speed = 2;
        game.foundHiddenEvents += 1;
    }

    private void initialiseAudio() {
        gameScreen.audioManager.pauseMusic();
        gameScreen.audioManager.playDungeonMusic();
    }

    private void initialiseCamera() {

        worldWidth = game.viewport.getWorldWidth();
        worldHeight = game.viewport.getWorldHeight();
        camera = new OrthographicCamera(worldWidth, worldHeight);
        camera.position.set(worldWidth / 2, worldHeight / 2, 0);
        game.viewport.setCamera(camera);

    }


    private void updateCamera(){
        if (player.sprite.getX()>worldWidth/2) {
            camera.position.set(player.sprite.getX(), camera.position.y, 0);
        }
    }


    @Override
    public void render(float delta) {

        // Clear to black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        if (Math.round(stateTime) % 12 == 0 && !previous && player.sprite.getX()<1300){
            gameScreen.audioManager.playRumble();
            previous = true;

        }

        if (Math.round(stateTime) % 12 != 0){
            previous = false;
        }

//=========================================================
        //Animation sequence

        if (!isPaused && player.sprite.getX()<1300) {
            player.handleInput(delta, gameScreen.playerSpeedModifier);
            player.updatePlayer(stateTime);
            updateCamera();
        }

        if (player.sprite.getX()>=1300) {
            gameScreen.audioManager.stopDungeonMusic();
            if (!didImpact){
                gameScreen.audioManager.playImpact();
                didImpact = true;
            }
            gameScreen.audioManager.stopFootsteps();
            pauseTimer += Gdx.graphics.getDeltaTime();
        }

        if (pauseTimer >= 5 && camera.position.x <= 1550){
            animation(delta);

        }

        if (pauseTimer >= 14){
            if (!didQuack){
                gameScreen.audioManager.playQuack();
                didQuack = true;
            }
        }

        if (didQuack && pauseTimer >= 16.5) {
            if (!didThunder) {
                gameScreen.audioManager.playThunder();
                didThunder = true;

            }
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            game.batch.begin();
            game.batch.draw(textIMG, 1250, 150, 642, 200 );
            game.batch.end();


            if (pauseTimer>=20){


                gameScreen.getPlayer().sprite.setPosition(1320,1170);
                for(String key: gameScreen.items.keySet()){
                    Collectable item = gameScreen.items.get(key);
                        if  (key.equals("phone")){
                            item.Collect();
                            gameScreen.numOfInventoryItems += 1;
                        }}
                buildingManager.exitBuilding();
            }
            return;
        }



        //=========================================================
        game.batch.begin();


        game.batch.draw(map, 0, 100, 1000, 200);
        game.batch.draw(map, 1000, 100, 1000, 200);
        game.batch.draw(LongBoi, 1800, 150, 150,150);
        player.sprite.draw(game.batch);


        game.batch.end();

        buildingManager.update(pauseTimer);
        stateTime += delta;




        //Keep player in bounds


        if (player.sprite.getY() > 280) {
            player.sprite.setY(279);
        }

        if (player.sprite.getY() < 100) {
            player.sprite.setY(101);
        }


    }


    private void animation(float delta){
        camera.translate(35*delta,0,0);
    }




    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
