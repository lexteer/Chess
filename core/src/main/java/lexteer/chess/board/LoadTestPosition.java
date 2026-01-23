package lexteer.chess.board;

import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;
import lexteer.chess.pieces.Piece;

public class LoadTestPosition {
    Board board;
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
            Piece pawn = new Piece(PieceType.PAWN, PieceColor.BLACK);
            board.set(i, pawn);
        }

        for(int i = 8; i < 16; i++) {
            Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);
            board.set(i, pawn);
        }
    }

    private void loadAllKnights() {
        Piece knight1 = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        Piece knight2 = new Piece(PieceType.KNIGHT, PieceColor.BLACK);

        Piece knight3 = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        Piece knight4 = new Piece(PieceType.KNIGHT, PieceColor.WHITE);

        board.set(57, knight1);
        board.set(62, knight2);
        board.set(1, knight3);
        board.set(6, knight4);
    }

    private void loadAllBishops() {
        Piece bishop1 = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        Piece bishop2 = new Piece(PieceType.BISHOP, PieceColor.BLACK);

        Piece bishop3 = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        Piece bishop4 = new Piece(PieceType.BISHOP, PieceColor.WHITE);

        board.set(58, bishop1);
        board.set(61, bishop2);
        board.set(2, bishop3);
        board.set(5, bishop4);
    }

    private void loadAllRooks() {
        Piece rook1 = new Piece(PieceType.ROOK, PieceColor.BLACK);
        Piece rook2 = new Piece(PieceType.ROOK, PieceColor.BLACK);

        Piece rook3 = new Piece(PieceType.ROOK, PieceColor.WHITE);
        Piece rook4 = new Piece(PieceType.ROOK, PieceColor.WHITE);

        board.set(56, rook1);
        board.set(63, rook2);
        board.set(0, rook3);
        board.set(7, rook4);
    }

    private void loadQueens() {
        Piece queen1 = new Piece(PieceType.QUEEN, PieceColor.BLACK);
        Piece queen2 = new Piece(PieceType.QUEEN, PieceColor.WHITE);

        board.set(59, queen1);
        board.set(3, queen2);
    }

    private void loadKings() {
        Piece king1 = new Piece(PieceType.KING, PieceColor.BLACK);
        Piece king2 = new Piece(PieceType.KING, PieceColor.WHITE);

        board.set(60, king1);
        board.set(4, king2);
    }
}
