package lexteer.chess.domain.board;

/*
    HASHING OF CHESS POSITIONS:
    - piece placement
    - side to move
    - castling rights
    - en passant file
 */

import lexteer.chess.domain.game.GameState;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.domain.piece.PieceColor;
import lexteer.chess.domain.piece.PieceType;

import java.util.SplittableRandom;

public final class Zobrist {

    private static final int KIND_COUNT = 12; // 6 white, 6 black

    private static final long[][] PIECE_SQUARE = new long[KIND_COUNT][64];
    private static final long SIDE_TO_MOVE;
    private static final long[] CASTLING = new long[4];
    private static final long[] EN_PASSANT_FILE = new long[8];

    // castling bit mask
    public static final int WK = 1;
    public static final int WQ = 2;
    public static final int BK = 4;
    public static final int BQ = 8;

    static {
        SplittableRandom rng = new SplittableRandom(0xC0FFEE_1234ABCDL);

        for (int k = 0; k < KIND_COUNT; k++) {
            for (int sq = 0; sq < 64; sq++) {
                PIECE_SQUARE[k][sq] = rng.nextLong();
            }
        }

        SIDE_TO_MOVE = rng.nextLong();

        for (int i = 0; i < 4; i++) {
            CASTLING[i] = rng.nextLong();
        }

        for (int f = 0; f < 8; f++) {
            EN_PASSANT_FILE[f] = rng.nextLong();
        }
    }

    private Zobrist() {}

    public static long compute(GameState state, PieceColor sideToMove) {
        Board board = state.board;
        long hash = 0L;

        for (int sq = 0; sq < 64; sq++) {
            Piece p = board.get(sq);
            if (p == null) continue;

            int kind = kindIndex(p);
            hash ^= PIECE_SQUARE[kind][sq];
        }

        if (sideToMove == PieceColor.BLACK) {
            hash ^= SIDE_TO_MOVE;
        }

        int castleMask = computeCastlingMask(state);
        if ((castleMask & WK) != 0) hash ^= CASTLING[0];
        if ((castleMask & WQ) != 0) hash ^= CASTLING[1];
        if ((castleMask & BK) != 0) hash ^= CASTLING[2];
        if ((castleMask & BQ) != 0) hash ^= CASTLING[3];

        int epFile = enPassantFileForHash(state, sideToMove);
        if(epFile != -1) {
            hash ^= EN_PASSANT_FILE[epFile];
        }

        return hash;
    }

    public static int computeCastlingMask(GameState state) {
        Board board = state.board;
        int mask = 0;

        // white
        Piece wK = board.get(4);
        if(isKingUnmoved(wK, PieceColor.WHITE)) {
            Piece wR_kingSide = board.get(7);
            if (isRookUnmoved(wR_kingSide, PieceColor.WHITE)) {
                mask |= WK;
            }

            Piece wR_queenSide = board.get(0);
            if (isRookUnmoved(wR_queenSide, PieceColor.WHITE)) {
                mask |= WQ;
            }
        }

        // black
        Piece bK = board.get(60);
        if(isKingUnmoved(bK, PieceColor.BLACK)) {
            Piece bR_kingSide = board.get(63);
            if (isRookUnmoved(bR_kingSide, PieceColor.BLACK)) {
                mask |= BK;
            }

            Piece bR_queenSide = board.get(56);
            if (isRookUnmoved(bR_queenSide, PieceColor.BLACK)) {
                mask |= BQ;
            }
        }

        return mask;
    }

    // returns true only if:
    // - piece isnt null
    // - type is king
    // - piece color is the specified color
    // - king has not moved
    public static boolean isKingUnmoved(Piece piece, PieceColor color) {
        if (piece == null) return false;
        if (piece.getType() != PieceType.KING) return false;
        if (piece.getColor() != color) return false;
        if (piece.hasMoved()) return false;
        return true;
    }

    public static boolean isRookUnmoved(Piece piece, PieceColor color) {
        if (piece == null) return false;
        if (piece.getType() != PieceType.ROOK) return false;
        if (piece.getColor() != color) return false;
        if (piece.hasMoved()) return false;
        return true;
    }

    public static int enPassantFileForHash(GameState state, PieceColor side) {
        int epSq = state.enPassantSquare;
        if(epSq < 0 || epSq >= 64) return -1;

        // if no pawn can capture en passant treat it as "-"
        if(!canAnyPawnCaptureEnPassant(state.board, side, epSq)) {
            return -1;
        }

        return epSq & 7; // file
    }

    private static boolean canAnyPawnCaptureEnPassant(Board board, PieceColor side, int epSq) {
        int file = epSq & 7;

        if(side == PieceColor.WHITE) {
            int fromLeft = epSq - 9;
            int fromRight = epSq - 7;

            Piece left = board.get(fromLeft);
            Piece right = board.get(fromRight);

            if(left == null || right == null) return false;

            if(file > 0 && left.getColor() == PieceColor.WHITE) return true;
            if(file < 7 && right.getColor() == PieceColor.WHITE) return true;
            return false;
        } else {
            int fromLeft = epSq + 7;
            int fromRight = epSq + 9;

            Piece left = board.get(fromLeft);
            Piece right = board.get(fromRight);

            if(left == null || right == null) return false;

            if(file > 0 && left.getColor() == PieceColor.BLACK) return true;
            if(file < 7 && right.getColor() == PieceColor.BLACK) return true;
            return false;
        }
    }

    private static int kindIndex(Piece piece) {
        // 0...5 white, 6...11 black
        int base = (piece.getColor() == PieceColor.WHITE) ? 0 : 6;

        return base + typeToOffset(piece.getType());
    }

    private static int typeToOffset(PieceType type) {
        return switch(type) {
            case PAWN -> 0;
            case KNIGHT -> 1;
            case BISHOP -> 2;
            case ROOK -> 3;
            case QUEEN -> 4;
            case KING -> 5;
        };
    }
}
