package lexteer.chess.main;

import lexteer.chess.board.Board;
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
}
