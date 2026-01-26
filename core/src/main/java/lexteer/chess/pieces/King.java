package lexteer.chess.pieces;

import lexteer.chess.board.Board;
import lexteer.chess.main.Move;
import lexteer.chess.main.enums.PieceType;

public final class King {

    // 8 neighboring squares
    private static final int[] DELTAS = {
        8, -8, 1, -1, 9, 7, -7, -9
    };

    public static int generatePseudo(Board board, int from, int[] outMoves) {
        Piece king = board.get(from);
        if (king == null || king.getType() != PieceType.KING) return 0;

        int count = 0;
        int fromFile = from & 7;

        for (int delta : DELTAS) {
            int to = from + delta;
            if (to < 0 || to >= 64) continue; // skip if outside the board

            // prevent horizontal/diagonal wrap-around
            int toFile = to & 7;
            int df = toFile - fromFile;
            if (df < -1 || df > 1) continue;

            // check if the target square is free
            Piece target = board.get(to);
            if (target == null) {
                outMoves[count++] = Move.make(from, to);
            } else if (target.getColor() != king.getColor()) {
                outMoves[count++] = Move.makeCapture(from, to);
            }
        }

        return count;
    }
}

