package lexteer.chess.domain.game;

import lexteer.chess.domain.board.Board;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.domain.piece.PieceColor;

public class InsufficientMaterial {
    // minor pieces count
    private static int whiteBishops;
    private static int blackBishops;
    private static int whiteKnights;
    private static int blackKnights;

    // bishop square color tracking
    private static int whiteLightBishops;
    private static int whiteDarkBishops;
    private static int blackLightBishops;
    private static int blackDarkBishops;

    public static boolean check(Board board) {
        reset();
        boolean sufficient = scanBoard(board);
        if(sufficient) return false;

        // king vs king
        if (whiteBishops + whiteKnights + blackBishops + blackKnights == 0) {
            return true;
        }

        // king + bishop vs king OR king + knight vs king
        if (whiteBishops + whiteKnights == 1 && blackBishops + blackKnights == 0) return true;
        if (blackBishops + blackKnights == 1 && whiteBishops + whiteKnights == 0) return true;

        // king + bishop vs king + bishops | same color bishops
        if (whiteKnights == 0 && blackKnights == 0 && whiteBishops == 1 && blackBishops == 1) {
            boolean bothLight = whiteLightBishops == 1 && blackLightBishops == 1;
            boolean bothDark = whiteDarkBishops == 1 && blackDarkBishops == 1;

            if (bothDark || bothLight) return true;
        }

        return false;
    }

    private static boolean scanBoard(Board board) {
        for(int sq = 0; sq < 64; sq++) {
            Piece piece = board.get(sq);
            if(piece == null) continue;

            switch (piece.getType()) {
                case PAWN, ROOK, QUEEN -> {
                    return true;
                }

                case BISHOP -> {
                    if(piece.getColor() == PieceColor.WHITE) {
                        whiteBishops++;
                        if(isLightSquare(sq)) whiteLightBishops++;
                        else whiteDarkBishops++;
                    } else {
                        blackBishops++;
                        if(isLightSquare(sq)) blackLightBishops++;
                        else blackDarkBishops++;
                    }
                }

                case KNIGHT -> {
                    if(piece.getColor() == PieceColor.WHITE) whiteKnights++;
                    else blackKnights++;
                }

                case KING -> {

                }
            }
        }
        return false;
    }

    private static void reset() {
        whiteBishops = 0;
        blackBishops = 0;
        whiteKnights = 0;
        blackKnights = 0;

        whiteLightBishops = 0;
        whiteDarkBishops = 0;
        blackLightBishops = 0;
        blackDarkBishops = 0;
    }

    private static boolean isLightSquare(int sq) {
        int rank = sq / 8;
        int file = sq % 8;
        return ((rank + file) & 1) == 0;
    }
}
