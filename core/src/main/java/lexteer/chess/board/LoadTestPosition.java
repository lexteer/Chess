package lexteer.chess.board;

import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;

public class LoadTestPosition {
    Board board;

    PieceColor black = PieceColor.BLACK;
    PieceColor white = PieceColor.WHITE;

    public LoadTestPosition(Board board) {
        this.board = board;

        loadAllPawns();
        loadAllKnights();
        loadAllBishops();
        loadAllRooks();
        loadQueens();
        loadKings();
    }

    private void loadAllPawns() {
        for(int i = 48; i < 56; i++) {
            board.initPiece(i, PieceType.PAWN, black);
        }

        for(int i = 8; i < 16; i++) {
            board.initPiece(i, PieceType.PAWN, white);
        }
    }

    private void loadAllKnights() {
        board.initPiece(57, PieceType.KNIGHT, black);
        board.initPiece(62, PieceType.KNIGHT, black);
        board.initPiece(1, PieceType.KNIGHT, white);
        board.initPiece(6, PieceType.KNIGHT, white);
    }

    private void loadAllBishops() {
        board.initPiece(58, PieceType.BISHOP, black);
        board.initPiece(61, PieceType.BISHOP, black);
        board.initPiece(2, PieceType.BISHOP, white);
        board.initPiece(5, PieceType.BISHOP, white);
    }

    private void loadAllRooks() {
        board.initPiece(56, PieceType.ROOK, black);
        board.initPiece(63, PieceType.ROOK, black);
        board.initPiece(0, PieceType.ROOK, white);
        board.initPiece(7, PieceType.ROOK, white);
    }

    private void loadQueens() {
        board.initPiece(59, PieceType.QUEEN, black);
        board.initPiece(3, PieceType.QUEEN, white);
    }

    private void loadKings() {
        board.initPiece(60, PieceType.KING, black);
        board.initPiece(4, PieceType.KING, white);
    }
}
