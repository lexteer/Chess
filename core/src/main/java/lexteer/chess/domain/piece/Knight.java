package lexteer.chess.domain.piece;

import lexteer.chess.domain.board.Board;
import lexteer.chess.domain.move.Move;

public final class Knight {

    private static final int[] DELTAS = {
        17,  1,
        15, -1,
        10,  2,
        6, -2,
        -6,  2,
        -10,-2,
        -15, 1,
        -17,-1
    };
    // returns the number of pseudo legal moves and fills them into outMoves int array
    // the moves outputted get encoded with the Move class
    public static int generatePseudo(Board board, int from, int[] outMoves) {
        Piece knight = board.get(from);
        if (knight == null || knight.getType() != PieceType.KNIGHT) return 0;

        int count = 0;
        int fromFile = from & 7; // from % 8

        // takes from DELTAS in pairs
        for (int i = 0; i < DELTAS.length; i += 2) {
            int delta1 = DELTAS[i]; // first num
            int delta2 = DELTAS[i + 1]; // second num

            int to = from + delta1;
            if (to < 0 || to >= 64) continue; // skip if outside board

            int toFile = to & 7; // target squares file
            // skip if squares file is not 1 or 2 files away from knight; for safety
            if (toFile != fromFile + delta2) continue;

            // check if theres a enemy piece on the target square
            Piece target = board.get(to);
            if (target == null) {
                outMoves[count++] = Move.make(from, to);
            } else if (target.getColor() != knight.getColor()) {
                outMoves[count++] = Move.makeCapture(from, to);
            }
        }

        return count;
    }

    public static boolean knightAttacks(int from, int target) {
        int df = Math.abs((from & 7) - (target & 7));
        int dr = Math.abs((from >>> 3) - (target >>> 3));
        return (df == 1 && dr == 2) || (df == 2 && dr == 1);
    }
}

