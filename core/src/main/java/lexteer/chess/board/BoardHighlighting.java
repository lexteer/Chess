package lexteer.chess.board;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lexteer.chess.main.GameScreen;
import lexteer.chess.main.Mouse;
import lexteer.chess.main.Move;
import lexteer.chess.pieces.Piece;

public class BoardHighlighting {
    BoardUi boardUi;
    GameScreen gameScreen;
    float squareSize;
    ShapeRenderer shapeRenderer;
    Camera camera;
    Mouse mouse;

    public BoardHighlighting(GameScreen gameScreen, BoardUi boardUi, Camera camera, Mouse mouse) {
        this.gameScreen = gameScreen;
        this.boardUi = boardUi;
        this.camera = camera;
        this.mouse = mouse;

        squareSize = boardUi.getSquareSize();

        shapeRenderer = new ShapeRenderer();
    }

    public void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);


        drawLegalMoves();

        // DEBUG: draw all squares attached by enemy
        //drawEnemySquares();

        shapeRenderer.end();
    }

    private void drawLegalMoves() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Piece selectedPiece = gameScreen.getSelectedPiece();
        if(selectedPiece == null) return;

        int mouseIndex = mouse.getIndex();

        int moveCount = gameScreen.getSelectionMoving().getMoveCount();
        int[] moveBuf = gameScreen.getSelectionMoving().getMoveBuf();

        for (int i = 0; i < moveCount; i++) {
            int to = Move.to(moveBuf[i]);

            if(mouseIndex != to) continue;

            float x = boardUi.getSquareX(to);
            float y = boardUi.getSquareY(to);

            shapeRenderer.setColor(new Color(0.96f, 0.96f, 0.92f, 0.35f));
            drawInsideBorder(x, y, squareSize, squareSize, 5);
        }
    }

    private void drawInsideBorder(float x, float y, float width, float height, float thickness) {
        if (thickness <= 0) return;

        // Clamp thickness so it never exceeds half size (prevents negative inner rects)
        thickness = Math.min(thickness, Math.min(width, height) * 0.5f);

        // Bottom
        shapeRenderer.rect(x, y, width, thickness);
        // Top
        shapeRenderer.rect(x, y + height - thickness, width, thickness);
        // Left
        shapeRenderer.rect(x, y + thickness, thickness, height - 2f * thickness);
        // Right
        shapeRenderer.rect(x + width - thickness, y + thickness, thickness, height - 2f * thickness);
    }

    private void drawEnemySquares() {
        boolean[] squares = gameScreen.getEnemyAttackedSquares();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for(int i = 0; i < squares.length; i++) {
            if(squares[i]) {
                float x = boardUi.getSquareX(i);
                float y = boardUi.getSquareY(i);
                shapeRenderer.setColor(1f, 0f, 0f, 0.5f);
                shapeRenderer.rect(x, y, squareSize, squareSize);
            }
        }
    }


    public void dispose() {
        shapeRenderer.dispose();
    }
}
