package lexteer.chess.pieces;

import lexteer.chess.main.GameState;
import lexteer.chess.main.Move;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;

public final class Pawn {

    private Pawn() {
    }

    public static int generatePseudo(GameState state, int from, int[] outMoves) {
        Piece pawn = state.board.get(from);
        if (pawn == null || pawn.getType() != PieceType.PAWN) return 0;

        final boolean white = pawn.getColor() == PieceColor.WHITE;
        final int dir = white ? 8 : -8;

        final int fromRank = from >>> 3;
        final int fromFile = from & 7;

        int count = 0;

        count = addForwardMoves(state, pawn, from, fromRank, dir, white, outMoves, count);
        count = addCaptureMoves(state, pawn, from, fromFile, dir, outMoves, count);
        count = addEnPassantMoves(state, pawn, from, fromRank, fromFile, dir, white, outMoves, count);

        return count;
    }

    private static int addForwardMoves(GameState state, Piece pawn, int from, int fromRank, int dir, boolean white, int[] outMoves, int count) {
        int one = from + dir;
        if (!isValidSquare(one) || state.board.get(one) != null) return count;

        // Forward 1
        outMoves[count++] = Move.make(from, one);

        // Forward 2 (only if on start rank and both squares empty)
        int startRank = white ? 1 : 6;
        if (fromRank == startRank) {
            int two = from + 2 * dir;
            if (isValidSquare(two) && state.board.get(two) == null) {
                outMoves[count++] = Move.make(from, two);
            }
        }

        return count;
    }

    private static int addCaptureMoves(GameState state, Piece pawn, int from, int fromFile, int dir, int[] outMoves, int count) {

        // Diagonal left capture
        if (fromFile > 0) {
            int capLeft = from + dir - 1;
            if (isValidSquare(capLeft)) {
                Piece target = state.board.get(capLeft);
                if (target != null && target.getColor() != pawn.getColor()) {
                    outMoves[count++] = Move.makeCapture(from, capLeft);
                }
            }
        }

        // Diagonal right capture
        if (fromFile < 7) {
            int capRight = from + dir + 1;
            if (isValidSquare(capRight)) {
                Piece target = state.board.get(capRight);
                if (target != null && target.getColor() != pawn.getColor()) {
                    outMoves[count++] = Move.makeCapture(from, capRight);
                }
            }
        }

        return count;
    }

    private static int addEnPassantMoves(GameState state, Piece pawn, int from, int fromRank, int fromFile, int dir, boolean white, int[] outMoves, int count) {
        int ep = state.enPassantSquare;
        if (ep == -1) return count;

        int epRank = ep >>> 3;
        int epFile = ep & 7;

        // Must be adjacent file
        if (Math.abs(epFile - fromFile) != 1) return count;

        // Your existing rank conditions (unchanged)
        if (white) {
            // white pawn from rank 4 -> ep on rank 5
            if (fromRank == 4 && epRank == 5) {
                outMoves[count++] = Move.make(from, ep, Move.ENPASSANT, 0);
            }
        } else {
            // black pawn from rank 3 -> ep on rank 2
            if (fromRank == 3 && epRank == 2) {
                outMoves[count++] = Move.make(from, ep, Move.ENPASSANT, 0);
            }
        }

        return count;
    }

    private static boolean isValidSquare(int sq) {
        return sq >= 0 && sq < 64;
    }
}
