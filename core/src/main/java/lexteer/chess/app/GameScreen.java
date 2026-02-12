package lexteer.chess.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import lexteer.chess.domain.board.FEN;
import lexteer.chess.domain.engine.EngineBinary;
import lexteer.chess.domain.engine.EngineController;
import lexteer.chess.domain.game.*;
import lexteer.chess.domain.move.RepetitionTracker;
import lexteer.chess.domain.move.SelectionMoving;
import lexteer.chess.domain.piece.PieceColor;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.domain.board.Board;
import lexteer.chess.ui.board.BoardCoords;
import lexteer.chess.ui.board.BoardHighlighting;
import lexteer.chess.ui.board.BoardUi;
import lexteer.chess.ui.board.PromotionGUI;
import lexteer.chess.ui.input.Mouse;

public class GameScreen implements Screen {

    private boolean engineEnabled = true;
    private final PieceColor playerColor = PieceColor.WHITE;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewPort;
    private BoardUi boardUi;
    private Board board;
    private BoardHighlighting boardHighlighting;
    private Mouse mouse;
    private SelectionMoving selectionMoving;
    private GameState state;
    private RepetitionTracker repetitionTracker;
    private BoardCoords boardCoords;

    private static PieceColor currentPlaying;

    private static boolean[] enemyAttackedSquares = new boolean[64];

    private boolean gameOver = false;
    private GameOver gameOverType;
    private Winner winner;

    // engine
    private EngineController engineController;
    private boolean engineThinking;

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
        boardCoords = new BoardCoords();

        FEN.load("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", state);

        state.updateZobristKey(PieceColor.WHITE); // initial hash
        repetitionTracker = new RepetitionTracker(state.getZobristKey());

        try {
            String enginePath = EngineBinary.prepareEnginePath();
            engineController = new EngineController(enginePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start stockfish", e);
        }
    }

    private void update(float delta) {
        if(!gameOver) {
            if (PromotionGUI.isOpen()) {
                PromotionGUI.update(mouse);
                mouse.justPressed = false;
                return;
            }

            if (!engineEnabled) {
                // 2 player mode; alternate selecting
                selectionMoving.update(currentPlaying);
            } else {
                if (currentPlaying != playerColor) {
                    // TODO: play engine move and switch player
                    if (engineController != null && !engineThinking) {
                        engineThinking = true;

                        int thinkMs = 400;

                        engineController.requestAndApplyMove(state, currentPlaying, thinkMs, () -> {
                            engineThinking = false;
                            switchPlayer();
                        });
                    }
                } else {
                    selectionMoving.update(playerColor);
                }
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
            boardCoords.draw(batch, boardUi);
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
        batch.dispose();
        if (engineController != null) {
            engineController.close();
        }
        state.dispose();
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

    public void setCurrentPlaying(PieceColor color) {
        currentPlaying = color;
    }

    public void switchPlayer() {
        Rules.generateEnemyControlledSquares(state, currentPlaying, enemyAttackedSquares);
        currentPlaying = (currentPlaying == PieceColor.BLACK) ? PieceColor.WHITE : PieceColor.BLACK;

        state.updateZobristKey(currentPlaying);
        repetitionTracker.addPosition(state.getZobristKey());

        checkGameOver(currentPlaying);
    }

    private void setGameOver(GameOver type, Winner winner) {
        gameOver = true;
        gameOverType = type;
        this.winner = winner;
        state.se.playGameOver();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void checkGameOver(PieceColor sideToMove) {
        Winner win = (sideToMove == PieceColor.BLACK) ? Winner.WHITE : Winner.BLACK;
        Winner draw = Winner.DRAW;

        if (Rules.isCheckMate(state, sideToMove)) {
            setGameOver(GameOver.CHECKMATE, win);
        }
        else if (Rules.isStaleMate(state, sideToMove)) {
            setGameOver(GameOver.STALEMATE, draw);
        }
        else if (state.getHalfMoveCounter() >= 100) {
            setGameOver(GameOver.FIFTYMOVES, draw);
        }
        else if (repetitionTracker.isThreeFold(state.getZobristKey())){
            setGameOver(GameOver.THREEFOLD, draw);
        }
        else if (InsufficientMaterial.check(board)) {
            setGameOver(GameOver.INSUFFICIENT, draw);
        }
    }
}
