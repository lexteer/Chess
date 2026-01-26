package lexteer.chess.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import lexteer.chess.board.Board;
import lexteer.chess.board.BoardUi;
import lexteer.chess.board.LoadTestPosition;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.pieces.Piece;

public class GameScreen implements Screen {

    private boolean engineEnabled = false;
    private final PieceColor playerColor = PieceColor.WHITE;

    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewPort;
    private BoardUi boardUi;
    private Board board;
    private Mouse mouse;
    private SelectionMoving selectionMoving;

    private PieceColor currentPlaying;

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
        selectionMoving = new SelectionMoving(mouse, board, boardUi, this);

        new LoadTestPosition(board);
    }

    private void update(float delta) {
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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
            board.drawPieces(batch);
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
    }

    public Piece getSelectedPiece() {
        return selectionMoving.getSelected();
    }

    public Board getBoard() {
        return board;
    }

    public void switchPlayer() {
        currentPlaying = (currentPlaying == PieceColor.BLACK) ? PieceColor.WHITE : PieceColor.BLACK;
    }
}
