package lexteer.chess.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import lexteer.chess.domain.game.GameState;
import lexteer.chess.domain.game.GameOver;
import lexteer.chess.domain.game.Rules;
import lexteer.chess.domain.move.SelectionMoving;
import lexteer.chess.domain.piece.PieceColor;
import lexteer.chess.domain.game.Winner;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.domain.board.Board;
import lexteer.chess.ui.board.BoardHighlighting;
import lexteer.chess.ui.board.BoardUi;
import lexteer.chess.ui.board.PromotionGUI;
import lexteer.chess.ui.input.Mouse;
import lexteer.chess.util.LoadTestPosition;

public class GameScreen implements Screen {

    private boolean engineEnabled = false;
    private final PieceColor playerColor = PieceColor.WHITE;

    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewPort;
    private BoardUi boardUi;
    private Board board;
    private BoardHighlighting boardHighlighting;
    private Mouse mouse;
    private SelectionMoving selectionMoving;
    private GameState state;

    private PieceColor currentPlaying;

    private static boolean[] enemyAttackedSquares = new boolean[64];


    private boolean gameOver = false;
    private GameOver gameOverType;
    private Winner winner;

    public GameScreen() {
        currentPlaying = PieceColor.WHITE; // white starts
    }

    @Override
    public void show() {
        // camera + viewport setup
        camera = new OrthographicCamera();
        viewPort = new FitViewport(800, 800, camera);
        viewPort.apply(true);
        camera.update();

        batch = new SpriteBatch();

        // instances
        boardUi = new BoardUi(camera, this);
        board = new Board(boardUi, this);
        mouse = new Mouse(camera, viewPort, boardUi);
        state = new GameState(this, board);
        selectionMoving = new SelectionMoving(mouse, board, boardUi, this, state);
        boardHighlighting = new BoardHighlighting(this, boardUi, camera, mouse);

        new LoadTestPosition(board);
    }

    private void update(float delta) {
        if(gameOver) {
            System.out.println(winner);
            return;
        }

        if(PromotionGUI.isOpen()) {
            PromotionGUI.update(mouse);
            mouse.justPressed = false;
            return;
        }

        if(!engineEnabled) {
            // 2 player mode; alternate selecting
            selectionMoving.update(currentPlaying);
        } else {
            if(currentPlaying != playerColor) {
                // TODO: play engine move and switch player
            } else {
                selectionMoving.update(playerColor);
            }
        }


        // reset at the end of all logic
        mouse.justPressed = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewPort.apply();
        camera.update();
        update(delta);


        boardUi.draw();
        boardHighlighting.draw();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
            board.drawPieces(batch);
            PromotionGUI.draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;

        viewPort.update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        boardUi.dispose();
        boardHighlighting.dispose();
    }

    public Piece getSelectedPiece() {
        return selectionMoving.getSelected();
    }

    public Board getBoard() {
        return board;
    }

    public SelectionMoving getSelectionMoving() {
        return selectionMoving;
    }

    public static boolean[] getEnemyAttackedSquares() {
        return enemyAttackedSquares;
    }

    public void switchPlayer() {
        Rules.generateEnemyControlledSquares(state, currentPlaying, enemyAttackedSquares);
        currentPlaying = (currentPlaying == PieceColor.BLACK) ? PieceColor.WHITE : PieceColor.BLACK;
        checkGameOver(currentPlaying);
    }

    private void checkGameOver(PieceColor sideToMove) {
        Winner win = (sideToMove == PieceColor.BLACK) ? Winner.WHITE : Winner.BLACK;

        if(Rules.isCheckMate(state, sideToMove)) {
            gameOver = true;
            gameOverType = GameOver.CHECKMATE;
            winner = win;
        }
        else if(Rules.isStaleMate(state, sideToMove)) {
            gameOver = true;
            gameOverType = GameOver.STALEMATE;
            winner = Winner.DRAW;
        }
        else if(state.getHalfMoveCounter() >= 100) {
            gameOver = true;
            gameOverType = GameOver.FIFTYMOVES;
            winner = Winner.DRAW;
        }

    }
}
