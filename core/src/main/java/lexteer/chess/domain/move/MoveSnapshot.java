package lexteer.chess.domain.move;

import lexteer.chess.domain.piece.Piece;

public final class MoveSnapshot {
    public int from, to, flags;

    public Piece movedPiece;
    public int movedOldSquare;

    public Piece capturedOnTo;

    // en passant
    public int epCapturedSq = -1;
    public Piece epCapturedPiece = null;

    // castling
    public int rookFrom = -1;
    public int rookTo = -1;
    public Piece rook = null;
    public int rookOldSquare = -1;

    // promotion
    public boolean wasPromotion = false;
    public Piece promotionPiece = null;
}
