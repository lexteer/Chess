package lexteer.chess.board;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lexteer.chess.main.GameScreen;
import lexteer.chess.main.Move;
import lexteer.chess.pieces.Piece;

public class BoardUi {

    public static int SQUARE_COUNT = 8; // 8x8 board
    private final float boardSizePercent = 0.75f;

    private final Color light = Color.valueOf("F0D9B5");
    private final Color dark = Color.valueOf("B58863");

    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final GameScreen gameScreen;

    public BoardUi(OrthographicCamera camera, GameScreen gamescreen) {
        this.camera = camera;
        this.gameScreen = gamescreen;

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

        highlightSelectedSquare();

        //drawLegalMoves();

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

    public float getSquareX(int index) {
        int file = index % 8;
        return getBoardX() + getSquareSize() * file;
    }

    public float getSquareY(int index) {
        return getBoardY() + (getSquareSize() * (index / 8)); // dont cast to float
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

    private void highlightSelectedSquare() {
        Piece selectedPiece = gameScreen.getSelectedPiece();
        if(selectedPiece == null) return;

        Board board = gameScreen.getBoard();

        float x = getSquareX(board.index(selectedPiece));
        float y = getSquareY(board.index(selectedPiece));

        shapeRenderer.setColor(new Color(0.98f, 0.78f, 0.22f, 0.85f));
        shapeRenderer.rect(x, y, getSquareSize(), getSquareSize());
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

}
