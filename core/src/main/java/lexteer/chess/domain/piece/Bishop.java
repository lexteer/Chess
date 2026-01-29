package lexteer.chess.domain.piece;

import lexteer.chess.domain.board.Board;
import lexteer.chess.domain.move.Move;
import lexteer.chess.domain.game.Rules;

public final class Bishop {

    private static final int NE = 9;
    private static final int NW = 7;
    private static final int SE = -7;
    private static final int SW = -9;

    private static final int[] DIRS = { NE, NW, SE, SW };

    private Bishop() {}

    public static int generatePseudo(Board board, int from, int[] outMoves, int start) {
        Piece bishop = board.get(from);
        if (bishop == null || bishop.getType() != PieceType.BISHOP) return 0;
        return generateDiagonal(board, from, bishop.getColor(), outMoves, start);
    }

    // same as the rook; check in Rook.java for explanation
    static int generateDiagonal(Board board, int from, PieceColor color, int[] outMoves, int start) {
        int count = 0;
        for (int dir : DIRS) {
            int sq = from;
            while (true) {
                int next = sq + dir;
                if (!isOnBoard(sq, next, dir)) break;

                Piece target = board.get(next);

                if (target == null) {
                    outMoves[start + count++] = Move.make(from, next);
                } else {
                    if (target.getColor() != color) {
                        outMoves[start + count++] = Move.makeCapture(from, next);
                    }
                    break;
                }
                sq = next;
            }
        }
        return count;
    }

    private static boolean isOnBoard(int from, int to, int dir) {
        if (to < 0 || to >= 64) return false;
        int fromFile = from & 7;
        int toFile = to & 7;
        if (dir == NE || dir == SE) return toFile == fromFile + 1;
        return toFile == fromFile - 1;
    }

    public static boolean bishopAttacks(Board board, int from, int target, boolean includeFirstBlocker) {
        int df = (target & 7) - (from & 7);
        int dr = (target >>> 3) - (from >>> 3);
        if (Math.abs(df) != Math.abs(dr)) return false;

        int step;
        if (df > 0 && dr > 0) step = 9;
        else if (df < 0 && dr > 0) step = 7;
        else if (df > 0 && dr < 0) step = -7;
        else step = -9;

        return Rules.ray(board, from, target, step, includeFirstBlocker);
    }
}


