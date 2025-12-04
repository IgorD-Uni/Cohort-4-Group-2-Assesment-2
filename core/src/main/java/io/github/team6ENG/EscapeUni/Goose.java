package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Represents and controls a goose
 */
public class Goose extends SpriteAnimations {

    private HashMap<String, Integer[]> animationInfo = new HashMap<String, Integer[]>();
    public boolean isFacingLeft = true;
    public boolean hasStolenTorch = false;
    public TextureRegion currentGooseFrame;
    private float speed = 0.75f;
    private int idleDistance = 20;
    public boolean isFlying;
    private TiledMapTileLayer.Cell cell;
    public Goose baby = null;
    public boolean attackModeActivated = false;
    public boolean isSleeping = false;
    private List<int[]> runPath =  Arrays.asList(new int[]{700, 400}, new int[]{340, 300}, new int[]{600, 150}, new int[]{550, 50});
    /**
     * Generate goose and its animations
     */
    public Goose() {
        super("sprites/goose.png", 15, 17);

        // HashMap<String, Integer[]> animationInfo:
        //      key - Name of animation
        //      Value - Array representing row of animation on sprite sheet and index of start and end frames
        animationInfo.put("walkLeft", new Integer[]{5,0,4});
        animationInfo.put("walkRight", new Integer[]{6,0,4});
        animationInfo.put("idleLeft", new Integer[]{16,0,5});
        animationInfo.put("idleRight", new Integer[]{15,0,5});
        animationInfo.put("flyRight", new Integer[]{11,0,10});
        animationInfo.put("flyLeft", new Integer[]{12,0,10});
        animationInfo.put("sleep", new Integer[]{13,5,10});


        generateAnimation(animationInfo,0.6f);


    }

    /**
     * Check if goose is obstructed
     * @param tileX y position of tile approaching
     * @param tileY y position of tile approaching
     * @return true if goose can move, else false
     */
    private boolean isMoveAllowed(int tileX, int tileY) {
        // check map boundaries
        if (tileX < 0 || tileY < 0 || tileX >= wallsLayer.getWidth() || tileY >= wallsLayer.getHeight()) {
            return false;
        }

        // check if tile is empty (null) or contains a wall
        cell = wallsLayer.getCell(tileX, tileY);
        if (cell == null || cell.getTile() == null) {
            return true;
        }

        return cell.getTile().getId() != mapWallsId;

    }

    /**
     * Move goose towards target and update animations
     * @param stateTime time in seconds since last frame
     * @param followX x of target
     * @param followY y of target
     * @param isPlayerMoving is player moving
     */
    public void moveGoose(float stateTime, float followX, float followY, boolean isPlayerMoving, boolean followIsSleeping) {

        int tileX = (int)(x+ getWidth() / 2) / tileDimensions;
        int tileY = (int)(y+ getHeight() / 2) / tileDimensions;
        currentGooseFrame = animations.get("sleep").getKeyFrame(stateTime, true);
        if(isSleeping){return;}

        float distance = (float) Math.sqrt(((x-followX) * (x-followX)) + ((y-followY)*(y-followY)));
        // If target is in range, idle
        if( distance <= idleDistance && !isPlayerMoving && isMoveAllowed(tileX, tileY) ) {
            if(isFacingLeft){
                currentGooseFrame = animations.get("idleLeft").getKeyFrame(stateTime, true);
            }
            else{
                currentGooseFrame = animations.get("idleRight").getKeyFrame(stateTime, true);
            }
            if(followIsSleeping){
                currentGooseFrame = animations.get("sleep").getKeyFrame(stateTime, true);

            }

        }
        else{
            isFlying = false;
            if (x > followX) {
                isFacingLeft = true;

                if ((!isMoveAllowed(tileX-1,tileY) && Math.abs(x-followX) > 50)|| !isMoveAllowed(tileX,tileY)) {
                    isFlying = true;
                    x -= speed;
                }
                else if (isMoveAllowed(tileX-1,tileY)){

                    x -= speed;
                }
            }
            else if(x <= followX -5) {
                isFacingLeft = false;
                if ( (!isMoveAllowed(tileX+1,tileY)&& Math.abs(x-followX) > 50) || !isMoveAllowed(tileX,tileY)){
                    isFlying = true;
                    x += speed;
                }
                else if(isMoveAllowed(tileX+1,tileY)){

                    x += speed;
                }
            }

            if(y > followY +5 ) {
                if ((!isMoveAllowed(tileX,tileY-1)&& Math.abs(y-followY) > 50)|| !isMoveAllowed(tileX,tileY)) {
                    isFlying = true;
                    y -= speed;
                }
                else if(isMoveAllowed(tileX, tileY-1)){
                    y-=speed;
                }
            }
            else if (y <= followY -5 ) {
                if((!isMoveAllowed(tileX ,tileY+1)&& Math.abs(y-followY) > 50)|| !isMoveAllowed(tileX,tileY)){
                    isFlying = true;
                    y += speed;
                }
                else if(isMoveAllowed(tileX ,tileY+1)){

                    y += speed;
                }
            }

            if(isFlying){
                if(isFacingLeft){
                    currentGooseFrame = animations.get("flyLeft").getKeyFrame(stateTime, true);

                }
                else{
                    currentGooseFrame = animations.get("flyRight").getKeyFrame(stateTime, true);
                }
            }
            else{
                if(isFacingLeft){
                    currentGooseFrame = animations.get("walkLeft").getKeyFrame(stateTime, true);

                }
                else{
                    currentGooseFrame = animations.get("walkRight").getKeyFrame(stateTime, true);
                }

            }
        }
    }

    /**
     * get width of goose frame
     * @return width of goose
     */
    public float getWidth() {
        return currentGooseFrame != null ? currentGooseFrame.getRegionWidth() : 16f; // default value
    }

    /**
     * get height of goose frame
     * @return height of goose
     */
    public float getHeight() {

        return currentGooseFrame != null ? currentGooseFrame.getRegionHeight() : 16f;
    }

    /**
     * Recursively generate a trail of baby geese
     * Each goose points to the next baby goose
     * Base case: Goose index, only generate 5 geese
     * @param gooseIndex number of current goose in the trail
     */
    public void loadBabyGoose(int gooseIndex){
        if (gooseIndex < 5) {
            baby = new Goose();

            baby.loadSprite(wallsLayer, mapWallsId, tileDimensions);
            baby.x = x;
            baby.y = y;
            baby.speed = speed*0.9f;

            baby.loadBabyGoose(gooseIndex + 1);

        }
    }

    /**
     * Goose steals torch
     */
    public void attackMode(){

        speed = 1.3f;
        idleDistance = 0;
        attackModeActivated = true;
    }

    /**
     * Path for goose to run with torch
     * @return destination coordinates
     */
    public int[] nextRunLocation(){
        if (Math.abs(x -runPath.get(0)[0]) <=5 && Math.abs(y - runPath.get(0)[1] ) <= 5&& runPath.size()>1) {

            runPath = runPath.subList(1,runPath.size());
        }
        else if(Math.abs(x -runPath.get(0)[0]) <=5 && Math.abs(y - runPath.get(0)[1] ) <= 5){
            isSleeping = true;
        }
        return runPath.get(0);

    }
}


