package lexteer.chess.domain.engine;

import lexteer.chess.domain.board.Board;
import lexteer.chess.domain.game.GameState;
import lexteer.chess.domain.move.Move;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.domain.piece.PieceType;

public final class UciToMove {
    private UciToMove() {}

    /**
     * Converts Stockfish UCI move (e.g. "e2e4", "e7e8q") into your encoded int.
     *
     * IMPORTANT:
     * - Requires current GameState to compute flags (capture, en-passant, castling).
     * - Throws if UCI is invalid or does not fit the current position.
     */
    public static int toMoveInt(String uci, GameState state) {
        Board board = state.board;

        if (uci == null) throw new IllegalArgumentException("UCI move is null");
        uci = uci.trim();

        // Stockfish sometimes returns "(none)" or "0000" in finished games.
        // You said you will not call it then, so failing hard is correct.
        if (uci.equals("(none)") || uci.equals("0000")) {
            throw new IllegalStateException("Engine returned no-move: " + uci);
        }

        if (uci.length() < 4) {
            throw new IllegalArgumentException("Invalid UCI: " + uci);
        }

        int from = squareToIndex(uci.substring(0, 2));
        int to   = squareToIndex(uci.substring(2, 4));

        // --- Determine flags from position ---
        int flags = 0;

        Piece moving = board.get(from);
        if (moving == null) {
            throw new IllegalStateException("No moving piece at " + uci.substring(0, 2) + " for UCI " + uci);
        }

        Piece target = board.get(to);
        boolean targetOccupied = target != null;

        // Castling: king moves two files (e1g1 / e1c1 / e8g8 / e8c8).
        // This is reliable and does not require extra state.
        if (moving.getType() == PieceType.KING && Math.abs(fileOf(to) - fileOf(from)) == 2) {
            flags |= Move.CASTLE;
        }

        // En-passant: pawn moves diagonally to an empty square.
        // Requires that your GameState tracks en-passant availability, otherwise
        // this heuristic is still correct for legal moves.
        if (moving.getType() == PieceType.PAWN
            && fileOf(from) != fileOf(to)
            && !targetOccupied) {
            // diagonal pawn move to empty target => en-passant capture
            flags |= (Move.CAPTURE | Move.ENPASSANT);
        } else if (targetOccupied) {
            flags |= Move.CAPTURE;
        }

        // Promotion (UCI has 5th char: q/r/b/n)
        if (uci.length() >= 5) {
            char p = uci.charAt(4);
            int promoCode = promoCharToCode(p);
            flags |= Move.PROMO;
            return Move.make(from, to, flags, promoCode);
        }

        // No promo
        if (flags == 0) {
            return Move.make(from, to);
        }
        return Move.make(from, to, flags, 0);
    }

    // ----------------- Square helpers -----------------

    // Assumes a1=0 ... h1=7, a2=8 ... h8=63
    public static int squareToIndex(String sq) {
        if (sq == null || sq.length() != 2) throw new IllegalArgumentException("Bad square: " + sq);
        char file = sq.charAt(0); // a..h
        char rank = sq.charAt(1); // 1..8
        int f = file - 'a';
        int r = rank - '1';
        if (f < 0 || f > 7 || r < 0 || r > 7) throw new IllegalArgumentException("Bad square: " + sq);
        return r * 8 + f;
    }

    private static int fileOf(int square) { return square & 7; }     // 0..7
    // private static int rankOf(int square) { return square >>> 3; } // 0..7 (unused)

    private static int promoCharToCode(char p) {
        return switch (Character.toLowerCase(p)) {
            case 'n' -> 2;
            case 'b' -> 1;
            case 'r' -> 3;
            case 'q' -> 4;
            default -> throw new IllegalArgumentException("Bad promotion char: " + p);
        };
    }
}

