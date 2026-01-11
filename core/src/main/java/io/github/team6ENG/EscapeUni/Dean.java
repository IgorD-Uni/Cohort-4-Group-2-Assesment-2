package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;

//Dean using a BFS algorithm to calculate best path, and to apply health damage
public class Dean extends SpriteAnimations {
    public float deanX;
    public float deanY;
    public float speed;
    public float normalSpeed = 0.25F;
    private boolean paused = false;
    private float pauseTimer = 0.0F;
    private float pathFindTimer = 0.0F;
    private int mapWidthTiles;
    private int mapHeightTiles;
    private boolean[][] collisionGrid;
    private float dx = 0.0F;
    private float dy = 0.0F;
    private float moveTimer = 2.0F;
    private boolean active = true;
    private LinkedList<Path> path;
    boolean canSee;

    private HashMap<String, Integer[]> animationInfo = new HashMap<String, Integer[]>();
    public TextureRegion currentDeanFrame;
    public boolean isFacingLeft = false;
    public boolean isFacingUp = false;

    //Dean creation following previous team format
    public Dean(String givenTexture, float x, float y, TiledMapTileLayer collisionLayer, int mapWallsId, int tileDimensions) {
        super(givenTexture, 8, 7);

        this.deanX = x;
        this.deanY = y;
        this.speed = this.normalSpeed;
        this.mapWidthTiles = collisionLayer.getWidth();
        this.mapHeightTiles = collisionLayer.getHeight();
        this.collisionGrid = extractCollisionGrid(collisionLayer, mapWallsId);

        animationInfo.put("idle", new Integer[]{0, 0, 8});
        animationInfo.put("walkForwards", new Integer[]{1, 0, 8});
        animationInfo.put("walkLeftForwards", new Integer[]{2, 0, 8});
        animationInfo.put("walkLeftBackwards", new Integer[]{3, 0, 8});
        animationInfo.put("walkBackwards", new Integer[]{4, 0, 8});
        animationInfo.put("walkRightBackwards", new Integer[]{5, 0, 8});
        animationInfo.put("walkRightForwards", new Integer[]{6, 0, 8});

        generateAnimation(animationInfo, 0.3F);
        loadSprite(collisionLayer, mapWallsId, tileDimensions);
    }

    //Map for positioning tiles and eligible movements
    private boolean[][] extractCollisionGrid(TiledMapTileLayer layer, int mapWallsId) {
        boolean[][] grid = new boolean[mapWidthTiles][mapHeightTiles];

        for (int xPosition = 0; xPosition < mapWidthTiles; ++xPosition) {
            for (int yPosition = 0; yPosition < mapHeightTiles; ++yPosition) {
                TiledMapTileLayer.Cell cell = layer.getCell(xPosition, yPosition);
                if (cell != null && cell.getTile() != null && cell.getTile().getId() == mapWallsId) {
                    grid[xPosition][yPosition] = true;
                }
            }
        }

        return grid;
    }

    public void update(float stateTime, float delta, Player player, HealthSystem healthSystem) {
        if (this.paused) {
            this.pauseTimer -= delta;
            if (this.pauseTimer <= 0.0F) {
                this.paused = false;
            }
            updateAnimation(stateTime);
            return;
        }

        float playerCentreX = player.sprite.getX() + player.sprite.getWidth() / 2;
        float playerCentreY = player.sprite.getY() + player.sprite.getHeight() / 2;

        int deanTileX = (int)(this.deanX / (float)tileDimensions);
        int deanTileY = (int)(this.deanY / (float)tileDimensions);
        int playerTileX = (int)(playerCentreX / (float)tileDimensions);
        int playerTileY = (int)(playerCentreY / (float)tileDimensions);

        float distance = this.getDistance(deanTileX, deanTileY, playerTileX, playerTileY);
        this.canSee = this.lineOfSight(deanTileX, deanTileY, playerTileX, playerTileY);

        if (this.canSee) {
            this.speed = 0.5F;
            this.pathFinding(delta, playerCentreX, playerCentreY);
        } else if (distance >= 10.0F) {
            this.speed = 0.25F;
            this.pathFinding(delta, playerCentreX, playerCentreY);
        } else if (distance < 10.0F) {
            this.speed = 0.6F;
            this.moveRandom(delta);
        }

        Rectangle deanBounds = new Rectangle(deanX - 5.0F, deanY - 5.0F, 10.0F, 10.0F);
        float playerHitX = player.sprite.getX() + (player.sprite.getWidth() / 2);
        float playerHitY = player.sprite.getY() + (player.sprite.getHeight() / 2);
        if (deanBounds.contains(playerHitX, playerHitY)) {
            healthSystem.takeDamage(10F);
            this.pause();
        }

        updateAnimation(stateTime);
    }

    //Animation for different directions as player and goose
    private void updateAnimation(float stateTime) {
        if (dx < 0) {
            isFacingLeft = true;
        } else if (dx > 0) {
            isFacingLeft = false;
        }

        if (dy > 0) {
            isFacingUp = true;
        } else if (dy < 0) {
            isFacingUp = false;
        }

        if (paused) {
            currentDeanFrame = animations.get("idle").getKeyFrame(stateTime, true);
        } else if (isFacingUp) {
            if (isFacingLeft) {
                currentDeanFrame = animations.get("walkLeftBackwards").getKeyFrame(stateTime, true);
            } else {
                currentDeanFrame = animations.get("walkRightBackwards").getKeyFrame(stateTime, true);
            }
        } else {
            if (isFacingLeft) {
                currentDeanFrame = animations.get("walkLeftForwards").getKeyFrame(stateTime, true);
            } else {
                currentDeanFrame = animations.get("walkRightForwards").getKeyFrame(stateTime, true);
            }
        }
    }

    private float getDistance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int result = dx * dx + dy * dy;
        return (float)Math.sqrt((double)result);
    }

    // If can see player, as above will have increase in speed
    private boolean lineOfSight(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            return true;
        } else {
            for (int i = 0; i <= steps; ++i) {
                float ratio = (float)i / (float)steps;
                int checkX = (int)((float)x1 + ratio * (float)(x2 - x1));
                int checkY = (int)((float)y1 + ratio * (float)(y2 - y1));
                if (checkX < 0) {
                    return false;
                }
                if (checkX >= this.mapWidthTiles) {
                    return false;
                }
                if (checkY < 0) {
                    return false;
                }
                if (checkY >= this.mapHeightTiles) {
                    return false;
                }
                if (this.collisionGrid[checkX][checkY]) {
                    return false;
                }
            }
            return true;
        }
    }

    //Calculation of path best to dean
    private void pathFinding(float delta, float playerX, float playerY) {
        int playerTileX = (int)(playerX / (float)tileDimensions);
        int playerTileY = (int)(playerY / (float)tileDimensions);
        int deanTileX = (int)(this.deanX / (float)tileDimensions);
        int deanTileY = (int)(this.deanY / (float)tileDimensions);

        this.pathFindTimer -= delta;

        if (this.pathFindTimer <= 0.0F || this.path == null || this.path.isEmpty()) {
            this.findPath(deanTileX, deanTileY, playerTileX, playerTileY);
            this.pathFindTimer = 0.5F;
        }

        if (this.path != null && !this.path.isEmpty()) {
            Path target = (Path)this.path.get(0);
            float targetX = (float)(target.x * tileDimensions) + (float)tileDimensions / 2.0F;
            float targetY = (float)(target.y * tileDimensions) + (float)tileDimensions / 2.0F;

            float diffX = targetX - this.deanX;
            float diffY = targetY - this.deanY;
            float dist = (float)Math.sqrt(diffX * diffX + diffY * diffY);

            if (dist < 2.0F) {
                this.path.remove(0);
            } else {
                this.dx = diffX / dist;
                this.dy = diffY / dist;
                this.deanX += this.dx * this.speed;
                this.deanY += this.dy * this.speed;
            }
        }
    }

    private void moveRandom(float delta) {
        this.moveTimer -= delta;
        if (this.moveTimer <= 0.0F) {
            int[][] directions = new int[][]{{0, 1}, {0, -1}, {-1, 0}, {1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            int randomIndex = (int)(Math.random() * (double)directions.length);
            float directionX = (float)directions[randomIndex][0];
            float directionY = (float)directions[randomIndex][1];
            float result = directionX * directionX + directionY * directionY;
            float length = (float)Math.sqrt((double)result);
            if (length > 0.0F) {
                this.dx = directionX / length;
                this.dy = directionY / length;
            }
            this.moveTimer = 2.0F;
        }

        float moveX = this.dx * this.speed;
        float moveY = this.dy * this.speed;
        float newX = this.deanX + moveX;
        float newY = this.deanY + moveY;
        int tileX = (int)(newX / (float)tileDimensions);
        int tileY = (int)(newY / (float)tileDimensions);

        if (tileX >= 0 && tileX < this.mapWidthTiles && tileY >= 0 && tileY < this.mapHeightTiles) {
            if (!this.collisionGrid[tileX][tileY]) {
                this.deanX = newX;
                this.deanY = newY;
            } else {
                int[][] newDirections = new int[][]{{0, 1}, {0, -1}, {-1, 0}, {1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                int newIndex = (int)(Math.random() * (double)newDirections.length);
                float newDirectionX = (float)newDirections[newIndex][0];
                float newDirectionY = (float)newDirections[newIndex][1];
                float newResult = newDirectionX * newDirectionX + newDirectionY * newDirectionY;
                float len = (float)Math.sqrt((double)newResult);
                if (len > 0.0F) {
                    this.dx = newDirectionX / len;
                    this.dy = newDirectionY / len;
                }
            }
        } else {
            int[][] newDirections = new int[][]{{0, 1}, {0, -1}, {-1, 0}, {1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            int newIndex = (int)(Math.random() * (double)newDirections.length);
            float newDirectionX = (float)newDirections[newIndex][0];
            float newDirectionY = (float)newDirections[newIndex][1];
            float newResult = newDirectionX * newDirectionX + newDirectionY * newDirectionY;
            float len = (float)Math.sqrt((double)newResult);
            if (len > 0.0F) {
                this.dx = newDirectionX / len;
                this.dy = newDirectionY / len;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (this.active) {
            if (currentDeanFrame != null) {
                batch.draw(currentDeanFrame, this.deanX - 24, this.deanY - 32, 48, 64);
            }
        }
    }

    private void pause() {
        this.paused = true;
        this.pauseTimer = 3.0F;
    }

    //Finding the best path of tiles around
    private void findPath(int startX, int startY, int endX, int endY) {
        LinkedList<Path> nodeCheck = new LinkedList();
        boolean[][] visitedTiles = new boolean[mapWidthTiles][mapHeightTiles];
        Path startingNode = new Path(startX, startY, null);
        nodeCheck.add(startingNode);
        visitedTiles[startX][startY] = true;

        int[][] optionsDirections = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        while (!nodeCheck.isEmpty()) {
            Path currentNode = nodeCheck.removeFirst();

            if (currentNode.x == endX && currentNode.y == endY) {
                LinkedList<Path> resultPath = new LinkedList();
                Path current = currentNode;
                while (current != null) {
                    resultPath.addFirst(current);
                    current = current.previous;
                }

                if (this.canSee) {
                    this.path = resultPath;
                    return;
                }

                this.pathFindTimer = 0.5F;
                this.path = resultPath;
                return;
            }

            for (int i = 0; i < 8; ++i) {
                int[] direction = optionsDirections[i];
                int adjacentX = currentNode.x + direction[0];
                int adjacentY = currentNode.y + direction[1];

                if (this.isWalkable(adjacentX, adjacentY) && !visitedTiles[adjacentX][adjacentY]) {
                    visitedTiles[adjacentX][adjacentY] = true;
                    Path newNode = new Path(adjacentX, adjacentY, currentNode);
                    nodeCheck.addLast(newNode);
                }
            }
        }
        path = null;
    }

    private class Path {
        int x;
        int y;
        Path previous;

        Path(int x, int y, Path previous) {
            this.x = x;
            this.y = y;
            this.previous = previous;
        }
    }

    private boolean isWalkable(int x, int y) {
        if (x < 0 || x >= mapWidthTiles || y < 0 || y >= mapHeightTiles) {
            return false;
        }
        return !collisionGrid[x][y];
    }

    public void dispose() {
        if (currentDeanFrame != null) {
            currentDeanFrame.getTexture().dispose();
        }
    }
}
