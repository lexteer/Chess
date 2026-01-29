package lexteer.chess.main;

import lexteer.chess.board.Board;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;
import lexteer.chess.pieces.Piece;

public class GameState {
    public final Board board;

    // special moves flags
    public int enPassantSquare = -1;
    // promotion
    public boolean promotionPending = false;
    public int pendingMove;
    public PieceColor pendingColor;

    public GameState(Board board) {
        this.board = board;
    }

    public void applyMove(int move) {
        // decode the move
        int from = Move.from(move);
        int to = Move.to(move);
        int flags = Move.flags(move);

        Piece pieceToMove = board.get(from);
        if(pieceToMove == null) return;

        // reset en passant square
        enPassantSquare = -1;

        enPassantCapture(flags, pieceToMove, to);

        handleCastling(flags, to);

        // normal move
        board.set(from, null); // clear "from"

        pieceToMove = checkPromotion(flags, pieceToMove, move); // modifies pieceToMove to exm. queen

        board.set(to, pieceToMove); // move piece
        pieceToMove.square = to; // update pieces square index
        board.centerPiece(pieceToMove);

        createEPSquare(from, to, pieceToMove);

        pieceToMove.pieceMoved();
    }

    // removes the pawn behind the target square
    private void enPassantCapture(int flags, Piece pieceToMove, int to) {
        if((flags & Move.ENPASSANT) != 0) {
            int capSq = (pieceToMove.getColor() == PieceColor.WHITE) ? (to - 8) : (to + 8);
            board.set(capSq, null);
        }
    }

    // created en passant square if move was a double pawn push
    // en passant square is the one that was jumped over when pawn pushed 2 squares
    private void createEPSquare(int from, int to, Piece pieceToMove) {
        if (pieceToMove.getType() == PieceType.PAWN) {
            int diff = to - from;
            if (diff == 16 || diff == -16) { // double push
                enPassantSquare = (from + to) / 2;
            }
        }
    }

    private Piece checkPromotion(int flags, Piece pieceToMove, int move) {
        if((flags & Move.PROMO) != 0) {
            PieceType type = PieceType.values()[Move.promo(move)];
            return new Piece(type, pieceToMove.getColor());
        }
        return pieceToMove;
    }

    private void handleCastling(int flags, int to) {
        if((flags & Move.CASTLE) == 0) return;

        int rookFrom = -1;
        int rookTo = -1;

        if (to == 6) { rookFrom = 7; rookTo = 5; }
        else if (to == 2) { rookFrom = 0;  rookTo = 3; }
        else if (to == 62) { rookFrom = 63; rookTo = 61; }
        else if (to == 58) { rookFrom = 56; rookTo = 59; }

        if(rookFrom == -1) return;

        Piece rook = board.get(rookFrom);
        if (rook == null) return;

        board.set(rookFrom, null);
        board.set(rookTo, rook);
        rook.square = rookTo;
        board.centerPiece(rook);

        rook.pieceMoved();
    }
}
