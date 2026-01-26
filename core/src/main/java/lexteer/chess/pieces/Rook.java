package lexteer.chess.pieces;

import lexteer.chess.board.Board;
import lexteer.chess.main.Move;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;

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
}

