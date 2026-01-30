package lexteer.chess.domain.piece;

import lexteer.chess.domain.game.GameState;
import lexteer.chess.domain.move.Move;

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

        count = addForwardMoves(state, from, fromRank, dir, white, outMoves, count);
        count = addCaptureMoves(state, pawn, from, white, fromFile, dir, outMoves, count);
        count = addEnPassantMoves(state, from, fromRank, fromFile, dir, white, outMoves, count);

        return count;
    }

    private static int addForwardMoves(GameState state, int from, int fromRank, int dir, boolean white, int[] outMoves, int count) {
        int one = from + dir;
        if (!isValidSquare(one) || state.board.get(one) != null) return count;

        // Forward 1
        if(isPromotionRank(white, one)) {
            outMoves[count++] = Move.make(from, one, Move.PROMO, 0);
            return count;
        } else {
            outMoves[count++] = Move.make(from, one);
        }

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

    private static int addCaptureMoves(GameState state, Piece pawn, int from, boolean white, int fromFile, int dir, int[] outMoves, int count) {

        // Diagonal left capture
        if (fromFile > 0) {
            int capLeft = from + dir - 1;
            if (isValidSquare(capLeft)) {
                Piece target = state.board.get(capLeft);
                if (target != null && target.getColor() != pawn.getColor()) {
                    if(isPromotionRank(white, capLeft)) {
                        outMoves[count++] = Move.make(from, capLeft, Move.CAPTURE | Move.PROMO, 0);
                    } else {
                        outMoves[count++] = Move.makeCapture(from, capLeft);
                    }
                }
            }
        }

        // Diagonal right capture
        if (fromFile < 7) {
            int capRight = from + dir + 1;
            if (isValidSquare(capRight)) {
                Piece target = state.board.get(capRight);
                if (target != null && target.getColor() != pawn.getColor()) {
                    if(isPromotionRank(white, capRight)) {
                        outMoves[count++] = Move.make(from, capRight, Move.CAPTURE | Move.PROMO, 0);
                    } else {
                        outMoves[count++] = Move.makeCapture(from, capRight);
                    }
                }
            }
        }

        return count;
    }

    private static int addEnPassantMoves(GameState state, int from, int fromRank, int fromFile, int dir, boolean white, int[] outMoves, int count) {
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

    private static boolean isPromotionRank(boolean white, int to) {
        int rank = to >>> 3;
        return white ? (rank == 7) : (rank == 0);
    }

    private static boolean isValidSquare(int sq) {
        return sq >= 0 && sq < 64;
    }

    // for controlled squares; only diagonal attacks
    public static boolean pawnAttacks(int from, PieceColor pawnColor, int target) {
        int file = from & 7;
        if (pawnColor == PieceColor.WHITE) {
            if (file > 0 && from + 7 == target) return true;
            if (file < 7 && from + 9 == target) return true;
        } else {
            if (file > 0 && from - 9 == target) return true;
            if (file < 7 && from - 7 == target) return true;
        }
        return false;
    }
}
