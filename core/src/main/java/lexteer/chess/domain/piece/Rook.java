package lexteer.chess.domain.piece;

import lexteer.chess.domain.board.Board;
import lexteer.chess.domain.move.Move;

import static lexteer.chess.domain.game.Rules.ray;

public class Rook {
    static final int NORTH = 8;
    static final int SOUTH = -8;
    static final int EAST = 1;
    static final int WEST = -1;

    static final int[] ROOK_DIRS = { NORTH, SOUTH, EAST, WEST };

    private Rook() {}

    public static int generatePseudo(Board board, int from, int[] outMoves, int start) {
        Piece rook = board.get(from);
        // check if piece is a rook
        if (rook == null || rook.getType() != PieceType.ROOK) return 0;
        // generate the moves
        return generateSliding(board, from, rook.getColor(), outMoves, start, ROOK_DIRS);
    }

    static int generateSliding(Board board, int from, PieceColor color, int[] outMoves, int start, int[] dirs) {
        int count = 0;
        // loop through all directions
        for (int dir : dirs) {
            // start from the starting square "from"
            int sq = from;
            while (true) {
                // add the direction num to the previous square
                int next = sq + dir;
                if (!isOnBoard(sq, next, dir)) break; // check if the pos is on board

                Piece target = board.get(next);

                // check if there is a piece on this square
                if (target == null) {
                    // if square empty -> plain move
                    outMoves[start + count++] = Move.make(from, next);
                } else {
                    // if enemy piece on the square -> capture move
                    if (target.getColor() != color) {
                        outMoves[start + count++] = Move.makeCapture(from, next);
                    }
                    break;
                }
                // move to the next square
                sq = next;
            }
        }
        return count;
    }

    private static boolean isOnBoard(int from, int to, int dir) {
        if (to < 0 || to >= 64) return false;
        if (dir == EAST || dir == WEST) return (from / 8) == (to / 8);
        return true;
    }

    public static boolean rookAttacks(Board board, int from, int target, boolean includeFirstBlocker) {
        int ff = from & 7, fr = from >>> 3;
        int tf = target & 7, tr = target >>> 3;

        if (fr == tr) {
            int step = (tf > ff) ? 1 : -1;
            return ray(board, from, target, step, includeFirstBlocker);
        }
        if (ff == tf) {
            int step = (tr > fr) ? 8 : -8;
            return ray(board, from, target, step, includeFirstBlocker);
        }
        return false;
    }
}

