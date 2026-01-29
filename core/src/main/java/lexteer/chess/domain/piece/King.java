package lexteer.chess.domain.piece;

import lexteer.chess.domain.board.Board;
import lexteer.chess.app.GameScreen;
import lexteer.chess.domain.move.Move;

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

        // --- CASTLING ---
        boolean[] attacked = GameScreen.getEnemyAttackedSquares();

        // If the king has moved, castling is never legal.
        if (!king.hasMoved()) {

            // White castling (king on e1)
            if (from == 4) {
                if (canCastle(board, attacked, 7, new int[]{5, 6}, new int[]{4, 5, 6}, king)) {
                    outMoves[count++] = Move.make(from, 6, Move.CASTLE, 0);
                }
                if (canCastle(board, attacked, 0, new int[]{3, 2, 1}, new int[]{4, 3, 2}, king)) {
                    outMoves[count++] = Move.make(from, 2, Move.CASTLE, 0);
                }
            }

            // Black castling (king on e8)
            if (from == 60) {
                if (canCastle(board, attacked, 63, new int[]{61, 62}, new int[]{60, 61, 62}, king)) {
                    outMoves[count++] = Move.make(from, 62, Move.CASTLE, 0);
                }
                if (canCastle(board, attacked, 56, new int[]{59, 58, 57}, new int[]{60, 59, 58}, king)) {
                    outMoves[count++] = Move.make(from, 58, Move.CASTLE, 0);
                }
            }
        }

        return count;
    }

    private static boolean canCastle(Board board, boolean[] attacked, int rookSq, int[] emptySquares, int[] kingPath, Piece king) {
        Piece rook = board.get(rookSq);
        if (rook == null) return false;
        if (rook.getColor() != king.getColor()) return false;
        if (rook.getType() != PieceType.ROOK) return false;

        // rook must not have moved
        if (rook.hasMoved()) return false;

        // squares between king and rook must be empty
        for (int sq : emptySquares) {
            if (board.get(sq) != null) return false;
        }

        // king may not castle out of / through / into check
        for (int sq : kingPath) {
            if (attacked[sq]) return false;
        }

        return true;
    }


    public static boolean kingAttacks(int from, int target) {
        int df = Math.abs((from & 7) - (target & 7));
        int dr = Math.abs((from >>> 3) - (target >>> 3));
        return df <= 1 && dr <= 1;
    }
}

