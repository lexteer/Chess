package lexteer.chess;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Board {
    private float screenWidth;
    private float screenHeight;

    private int squareSize;
    public static int SQUARE_COUNT = 8; // 8x8 board
    private float boardSize = 0.75f; // percentage of screen (70% of screen)

    private ShapeRenderer shapeRenderer;
    OrthographicCamera camera;

    public Board(OrthographicCamera camera) {
        this.camera = camera;
        this.screenHeight = camera.viewportHeight;
        this.screenWidth = camera.viewportWidth;

        shapeRenderer = new ShapeRenderer();
    }

    public void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // draw the light square
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(getBoardX(), getBoardY(), getBoardSize(), getBoardSize());

        // draw the dark squares
        drawDarkSquares();

        shapeRenderer.end();
    }

    public float getBoardSize() {
        float width = camera.viewportWidth * boardSize;
        float height = camera.viewportHeight * boardSize;

        return Math.min(width, height);
    }

    public int getBoardX() {
        float width = camera.viewportWidth;
        return (int) ((width/2) - (getBoardSize()/2));
    }

    public int getBoardY() {
        float height = camera.viewportHeight;
        return (int) ((height/2) - (getBoardSize()/2));
    }

    public float getSquareSize() {
        return getBoardSize() / SQUARE_COUNT;
    }

    private void drawDarkSquares() {
        shapeRenderer.setColor(Color.BLACK);

        for(int row = 0; row < SQUARE_COUNT; row++) {
            for (int col = 0; col < SQUARE_COUNT; col += 2) {
                float x = getBoardX() + col * getSquareSize();
                float y = getBoardY() + row * getSquareSize();

                // checker board pattern
                if(row % 2 != 0) {
                    x += getSquareSize();
                }

                shapeRenderer.rect(x, y, getSquareSize(), getSquareSize());
            }
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

}
