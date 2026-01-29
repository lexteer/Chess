package lexteer.chess.domain.piece;

import lexteer.chess.domain.board.Board;

public class Queen {
    public static int generatePseudo(Board board, int from, int[] outMoves) {
        Piece queen = board.get(from);
        if (queen == null || queen.getType() != PieceType.QUEEN) return 0;

        int count = 0;

        // rook + bishop
        count += Rook.generateSliding(board, from, queen.getColor(), outMoves, count, Rook.ROOK_DIRS);
        count += Bishop.generateDiagonal(board, from, queen.getColor(), outMoves, count);

        return count;
    }
}
