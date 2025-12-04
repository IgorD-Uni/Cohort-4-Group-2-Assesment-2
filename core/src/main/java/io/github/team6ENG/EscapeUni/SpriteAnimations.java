package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Cuts sprite sheets up into animations
 */
public class SpriteAnimations  {

    public HashMap<String, Animation<TextureRegion>> animations = new HashMap<String, Animation<TextureRegion>>();
    public float x;
    public float y;
    private HashMap<String, Integer[]> animationInfo = new HashMap<String, Integer[]>();
    private final int COLUMNS;
    private final int ROWS;
    private final Texture sheet;


    protected TiledMapTileLayer wallsLayer;
    protected int mapWallsId;
    protected int tileDimensions;


    /**
     * Loads sprite sheet
     * @param file path of sprite sheet
     * @param SheetCols Number of columns in sheet
     * @param SheetRows Number of rows in sheet
     */
    public SpriteAnimations(String file, int SheetCols, int SheetRows) {
        COLUMNS = SheetCols;
        ROWS = SheetRows;
        sheet= new Texture(Gdx.files.internal(file));
    }

    /**
     * Stores map info for later boundary detection
     * @param walls tilemap of game walls
     * @param id tilemap layer which represents the walls
     * @param d dimentions of each tile on the map i.e. 8 or 16
     */
    public void loadSprite(TiledMapTileLayer walls, int id, int d) {
        wallsLayer = walls;
        mapWallsId = id;
        tileDimensions = d;
    }

    /**
     * Cuts the sprite sheet up
     * Stores resulting animations into animations dictionary
     * @param animInfo dictionary of sprite sheet layout:
     *                 key - Name of animation
     *                 Value - Array representing row of animation on sprite sheet and index of start and end frames
     * @param animationSpeed number of seconds each animation frame should last
     */
    protected void generateAnimation( HashMap<String, Integer[]> animInfo, float animationSpeed){
        //animationInfo [0] = Row of animation, [1] = start frame [2] = end frame
        animationInfo = animInfo;

        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / COLUMNS,
            sheet.getHeight() / ROWS);

        TextureRegion[] frames;
        for(String key : animationInfo.keySet())
        {
            frames = new TextureRegion[animationInfo.get(key)[2]- animationInfo.get(key)[1]];
            for (int i = animationInfo.get(key)[1]; i < animationInfo.get(key)[2]; i++ ){
                frames[i - animationInfo.get(key)[1]] = tmp[animationInfo.get(key)[0]][i];
            }
            animations.put(key, new Animation<TextureRegion>(animationSpeed, frames));

        }
    }

     public void dispose() {
        if (sheet != null) {
            sheet.dispose();
        }

    }


}
