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
import lexteer.chess.pieces.Piece;
import lexteer.chess.pieces.PieceSelection;

public class GameScreen implements Screen {

    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewPort;
    private BoardUi boardUi;
    private Board board;
    private Mouse mouse;

    private PieceSelection pieceSelection;

    public GameScreen() {

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
        pieceSelection = new PieceSelection(mouse, board, boardUi);

        new LoadTestPosition(board);
    }

    private void update(float delta) {
        pieceSelection.update();
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
        return pieceSelection.getSelected();
    }

    public Board getBoard() {
        return board;
    }
}
