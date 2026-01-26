package lexteer.chess.pieces;

import com.badlogic.gdx.math.Vector2;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;

public class Piece {
    private PieceType type;
    private PieceColor color;
    private Vector2 position = new Vector2();
    public int square = -1;

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public void updatePosition(float x, float y) {
        position.x = x;
        position.y = y;
    }

    public Vector2 getPosition() {
        return position;
    }

}
