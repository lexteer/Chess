package lexteer.chess.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import lexteer.chess.board.BoardUi;

public class Mouse extends InputAdapter {
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final BoardUi boardUi;
    private final Vector3 tmp = new Vector3();

    public Vector2 position = new Vector2();
    public boolean pressed;
    public boolean dragging;

    public Mouse(OrthographicCamera camera, Viewport viewport, BoardUi boardUi) {
        this.camera = camera;
        this.viewport = viewport;
        this.boardUi = boardUi;
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        position.x = screenX;
        position.y = screenY;

        pressed = true;
        dragging = false;

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        position.x = screenX;
        position.y = screenY;

        pressed = false;
        dragging = false;

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        position.x = screenX;
        position.y = screenY;

        dragging = true;
        pressed = true;
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        position.x = screenX;
        position.y = screenY;

        return false;
    }

    // position with 0,0 at bottom left for consistency
    public float getX() {
        tmp.set(position.x, position.y, 0);
        viewport.unproject(tmp);
        return tmp.x;
    }

    public float getY() {
        tmp.set(position.x, position.y, 0);
        viewport.unproject(tmp);
        return tmp.y;
    }

    public boolean isOutsideTheBoard() {
        float boardX = boardUi.getBoardX();
        float boardY = boardUi.getBoardY();
        float boardSize = boardUi.getBoardSize();

        // horizontal check
        if(getX() < boardX || getX() > boardX + boardSize) return true;

        // vertical check
        if(getY() < boardY || getY() > boardY + boardSize) return true;

        return false;
    }

    // -1 if not on valid square
    public int getIndex() {
        if(isOutsideTheBoard()) return -1;

        float boardX = boardUi.getBoardX();
        float boardY = boardUi.getBoardY();
        float squareSize = boardUi.getSquareSize();

        int file = (int)((getX() - boardX) / squareSize);
        int rank = (int)((getY() - boardY) / squareSize);

        return rank * 8 + file;
    }
}
