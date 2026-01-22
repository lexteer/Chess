package lexteer.chess;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Board {

    public static int SQUARE_COUNT = 8; // 8x8 board
    private final float boardSizePercent = 0.75f;

    private final Color light = Color.valueOf("F0D9B5");
    private final Color dark = Color.valueOf("B58863");

    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;

    public Board(OrthographicCamera camera) {
        this.camera = camera;

        shapeRenderer = new ShapeRenderer();
    }

    public void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float boardSize = getBoardSize();
        float squareSize = getSquareSize();
        float boardX = getBoardX();
        float boardY = getBoardY();

        // draw the light square
        shapeRenderer.setColor(light);

        shapeRenderer.rect(boardX, boardY, boardSize, boardSize);

        // draw the dark squares
        drawDarkSquares(squareSize, boardX, boardY);

        shapeRenderer.end();
    }

    public float getBoardSize() {
        float width = camera.viewportWidth * boardSizePercent;
        float height = camera.viewportHeight * boardSizePercent;

        return Math.min(width, height);
    }

    public float getBoardX() {
        float width = camera.viewportWidth;
        return (width/2) - (getBoardSize()/2);
    }

    public float getBoardY() {
        float height = camera.viewportHeight;
        return (height/2) - (getBoardSize()/2);
    }

    public float getSquareSize() {
        return getBoardSize() / SQUARE_COUNT;
    }

    private void drawDarkSquares(float squareSize, float boardX, float boardY) {
        shapeRenderer.setColor(dark);

        for(int row = 0; row < SQUARE_COUNT; row++) {
            for (int col = 0; col < SQUARE_COUNT; col += 2) {
                float x = boardX + col * squareSize;
                float y = boardY + row * squareSize;

                // checker board pattern
                if(row % 2 != 0) {
                    x += squareSize;
                }

                shapeRenderer.rect(x, y, squareSize, squareSize);
            }
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

}
