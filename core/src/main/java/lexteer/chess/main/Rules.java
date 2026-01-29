package lexteer.chess.main;

import lexteer.chess.board.Board;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.pieces.*;

public class Rules {
    private static int[] tmp = new int[64];

    public static int generateLegalMoves(GameState state, int from, int[] out) {
        Board board = state.board;
        Piece piece = board.get(from);
        if(piece == null) return 0;

        // pseudo-legal moves
        int pseudoCount = switch (piece.getType()) {
            case ROOK -> Rook.generatePseudo(board, from, tmp, 0);
            case BISHOP -> Bishop.generatePseudo(board, from, tmp, 0);
            case KNIGHT -> Knight.generatePseudo(board, from, tmp);
            case QUEEN -> Queen.generatePseudo(board, from, tmp);
            case KING -> King.generatePseudo(board, from, tmp);
            case PAWN -> Pawn.generatePseudo(state, from, tmp);
        };

        // legality filter
        int legalCount = 0;
        for(int i = 0; i < pseudoCount; i++) {
            int mv = tmp[i];
            if(!moveLeavesKingInCheck(state, mv, board.getKing(piece.getColor()))) {
                out[legalCount++] = mv;
            }
        }
        return legalCount;
    }

    private static boolean moveLeavesKingInCheck(GameState state, int move, Piece king) {
        Board board = state.board;
        // apply move
        MoveSnapshot snap = state.applyMoveLite(move);
        // check if king in check
        boolean kingInCheck = isKingInCheck(state, king);
        // undo move
        state.undoMoveLite(snap);
        // return result
        return kingInCheck;
    }

    public static boolean isKingInCheck(GameState state, Piece king) {
        PieceColor enemy = (king.getColor() == PieceColor.BLACK) ? PieceColor.WHITE :PieceColor.BLACK;
        return canEnemyMoveHere(state, king.square, enemy);
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

    public static int countAllLegalMoves(GameState state, PieceColor side, int[] buffer) {
        int count = 0;

        for(int sq = 0; sq < 64; sq++) {
            Piece piece = state.board.get(sq);
            if(piece == null || piece.getColor() != side) continue;

            count += generateLegalMoves(state, sq, buffer);
        }

        return count;
    }

    public static boolean isCheckMate(GameState state, PieceColor sideToMove) {
        int[] tmp = new int[256];
        boolean noMoves = countAllLegalMoves(state, sideToMove, tmp) == 0;
        return noMoves && isKingInCheck(state, state.board.getKing(sideToMove));
    }

    public static boolean isStaleMate(GameState state, PieceColor sideToMove) {
        int[] tmp = new int[256];
        boolean noMoves = countAllLegalMoves(state, sideToMove, tmp) == 0;
        return noMoves && !isKingInCheck(state, state.board.getKing(sideToMove));
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
