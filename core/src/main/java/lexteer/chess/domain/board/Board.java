package lexteer.chess.domain.board;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import lexteer.chess.assets.Assets;
import lexteer.chess.app.GameScreen;
import lexteer.chess.domain.piece.PieceColor;
import lexteer.chess.domain.piece.PieceType;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.ui.board.BoardUi;

public class Board {
    Piece[] squares;

    BoardUi boardUi;
    GameScreen gameScreen;

    public Board(BoardUi boardUi, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.boardUi = boardUi;

        squares = new Piece[64];
    }

    public static int index(File file, int rank) {
        return rank * 8 + file.getValue();
    }

    public int index(Piece piece) {
        for(int i = 0; i < 64; i++) {
            if(squares[i] == piece) {
                return i;
            }
        }
        return -1;
    }

    public static File getFile(int index) {
        int fileValue = index % 8;
        return File.getFileLetter(fileValue);
    }

    public static int getRank(int index) {
        return index / 8;
    }

    public Piece get(int index) {
        if(index == -1) return null;
        return squares[index];
    }

    public boolean isSquareEmpty(int index) {
        return squares[index] == null;
    }

    public void initPiece(int index, PieceType type, PieceColor color) {
        if(index < 0 || index > 63) return;

        Piece newPiece = new Piece(type, color);

        squares[index] = newPiece;
        newPiece.square = index;

        centerPiece(newPiece);
    }

    public void centerPiece(Piece piece) {
        if(piece.square == -1) return;
        float x = boardUi.getBoardX() + (getFile(piece.square).getValue() * boardUi.getSquareSize());
        float y = boardUi.getBoardY() + (getRank(piece.square) * boardUi.getSquareSize());

        piece.updatePosition(x, y);
    }

    public void set(int index, Piece piece) {
        squares[index] = piece;
    }

    public Piece getKing(PieceColor color) {
        for(Piece piece : squares) {
            if(piece == null || piece.getColor() != color) continue;

            if(piece.getType() == PieceType.KING) {
                return piece;
            }
        }
        return null;
    }

    public void drawPieces(Batch batch) {
        Piece selected = null;
        Texture selectedTex = null;
        for(int i = 0; i < 64; i++) {
            Piece p = squares[i];
            if(p == null) continue;

            Texture image = null;

            try{
                image = Assets.get(getPieceTexturePath(squares[i]), Texture.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Piece select = gameScreen.getSelectedPiece();
            if(p == select) {
                selected = select;
                selectedTex = image;
            }

            if(image == null) continue;

            batch.draw(image, p.getPosition().x, p.getPosition().y, boardUi.getSquareSize(), boardUi.getSquareSize());
        }
        // draw selected piece on top of everyone
        if(selected != null && selectedTex != null) {
            batch.draw(selectedTex, selected.getPosition().x, selected.getPosition().y, boardUi.getSquareSize(), boardUi.getSquareSize());
        }
    }

    private String getPieceTexturePath(Piece p) {
        boolean white = p.getColor() == PieceColor.WHITE;
        return switch (p.getType()) {
            case KNIGHT -> white ? "pieces/white/knight.png" : "pieces/black/knight.png";
            case BISHOP -> white ? "pieces/white/bishop.png" : "pieces/black/bishop.png";
            case ROOK -> white ? "pieces/white/rook.png" : "pieces/black/rook.png";
            case QUEEN -> white ? "pieces/white/queen.png" : "pieces/black/queen.png";
            case KING -> white ? "pieces/white/king.png" : "pieces/black/king.png";
            default -> white ? "pieces/white/pawn.png" : "pieces/black/pawn.png";
        };
    }
}
