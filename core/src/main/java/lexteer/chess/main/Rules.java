package lexteer.chess.main;

import lexteer.chess.board.Board;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.pieces.*;

public class Rules {

    public static int generateLegalMoves(GameState state, int from, int[] out) {
        Board board = state.board;
        Piece piece = board.get(from);
        if(piece == null) return 0;

        return switch (piece.getType()) {
            case ROOK -> Rook.generatePseudo(board, from, out, 0);
            case BISHOP -> Bishop.generatePseudo(board, from, out, 0);
            case KNIGHT -> Knight.generatePseudo(board, from, out);
            case QUEEN -> Queen.generatePseudo(board, from, out);
            case KING -> King.generatePseudo(board, from, out);
            case PAWN -> Pawn.generatePseudo(state, from, out);
        };
    }

    public static void generateEnemyControlledSquares(GameState state, PieceColor enemyColor, boolean[] out) {
        for (int i = 0; i < 64; i++) out[i] = false;

        for(int sq = 0; sq < 64; sq++) {
            if(canEnemyMoveHere(state, sq, enemyColor)) {
                out[sq] = true;
            }
        }
    }

    // pseudo legal moves check
    // returns true even if enemy would have to leave king in check to move "here"
    public static boolean canEnemyMoveHere(GameState state, int target, PieceColor enemyColor) {
        Board board = state.board;

        Piece onTarget = board.get(target);
        boolean targetHasEnemyPiece = (onTarget != null && onTarget.getColor() == enemyColor);

        for (int from = 0; from < 64; from++) {
            Piece p = board.get(from);
            if (p == null || p.getColor() != enemyColor) continue;

            // If the target has an enemy piece, that piece cannot "defend itself".
            if (targetHasEnemyPiece && from == target) continue;

            switch (p.getType()) {
                case PAWN -> {
                    if (Pawn.pawnAttacks(from, enemyColor, target)) return true;
                }
                case KNIGHT -> {
                    if (Knight.knightAttacks(from, target)) return true;
                }
                case KING -> {
                    if (King.kingAttacks(from, target)) return true;
                }
                case ROOK -> {
                    if (Rook.rookAttacks(board, from, target, /*includeFirstBlocker=*/targetHasEnemyPiece)) return true;
                }
                case BISHOP -> {
                    if (Bishop.bishopAttacks(board, from, target, /*includeFirstBlocker=*/targetHasEnemyPiece)) return true;
                }
                case QUEEN -> {
                    if (Rook.rookAttacks(board, from, target, targetHasEnemyPiece)
                        || Bishop.bishopAttacks(board, from, target, targetHasEnemyPiece)) return true;
                }
            }
        }

        return false;
    }

    public static boolean ray(Board board, int from, int target, int step, boolean includeFirstBlocker) {
        int sq = from + step;

        while (sq >= 0 && sq < 64) {
            // stop file wrap for horizontal/diagonal steps
            if (step == 1 || step == -1 || step == 7 || step == -7 || step == 9 || step == -9) {
                int prevFile = (sq - step) & 7;
                int file = sq & 7;
                if (Math.abs(file - prevFile) != 1) return false;
            }

            if (sq == target) return true;

            if (board.get(sq) != null) {
                // If we want "defended enemy occupant", allow the first blocker square (the target) only.
                return includeFirstBlocker && sq == target;
            }

            sq += step;
        }

        return false;
    }

}
