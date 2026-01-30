package lexteer.chess.domain.game;

import lexteer.chess.domain.board.Board;
import lexteer.chess.app.GameScreen;
import lexteer.chess.domain.board.Zobrist;
import lexteer.chess.domain.move.Move;
import lexteer.chess.domain.move.MoveSnapshot;
import lexteer.chess.domain.piece.PieceColor;
import lexteer.chess.domain.piece.PieceType;
import lexteer.chess.domain.piece.Piece;

public class GameState {
    public final GameScreen gameScreen;
    public final Board board;

    // special moves flags
    public int enPassantSquare = -1;
    // promotion
    public boolean promotionPending = false;
    public int pendingMove;
    public PieceColor pendingColor;

    // special rules
    public int halfMoveClock = 0;

    private long zobristKey;
    public int fullMoves = 1;

    public GameState(GameScreen gameScreen, Board board) {
        this.gameScreen = gameScreen;
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

        updateFiftyMoveRuleCounter(pieceToMove, flags);

        // normal move
        board.set(from, null); // clear "from"

        pieceToMove = checkPromotion(flags, pieceToMove, move); // modifies pieceToMove to exm. queen

        board.set(to, pieceToMove); // move piece
        pieceToMove.square = to; // update pieces square index
        board.centerPiece(pieceToMove);

        createEPSquare(from, to, pieceToMove);

        pieceToMove.pieceMoved();

        if (pieceToMove.getColor() == PieceColor.BLACK) {
            fullMoves++;
        }
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

    // METHODS FOR TEMP MOVES FOR LEGALITY CHECKS
    public MoveSnapshot applyMoveLite(int move) {
        MoveSnapshot s = new MoveSnapshot();

        s.from = Move.from(move);
        s.to = Move.to(move);
        s.flags = Move.flags(move);

        Piece moving = board.get(s.from);
        if (moving == null) return s; // snapshot will be mostly empty; caller can handle

        s.movedPiece = moving;
        s.movedOldSquare = moving.square;

        s.capturedOnTo = board.get(s.to);

        // --- En passant capture: captured pawn is behind "to" ---
        if ((s.flags & Move.ENPASSANT) != 0 && moving.getType() == PieceType.PAWN) {
            s.epCapturedSq = (moving.getColor() == PieceColor.WHITE) ? (s.to - 8) : (s.to + 8);
            s.epCapturedPiece = board.get(s.epCapturedSq);
        }

        // --- Castling rook shift ---
        if ((s.flags & Move.CASTLE) != 0 && moving.getType() == PieceType.KING) {
            if (s.to == 6) { s.rookFrom = 7;  s.rookTo = 5; }
            else if (s.to == 2) { s.rookFrom = 0;  s.rookTo = 3; }
            else if (s.to == 62) { s.rookFrom = 63; s.rookTo = 61; }
            else if (s.to == 58) { s.rookFrom = 56; s.rookTo = 59; }

            if (s.rookFrom != -1) {
                s.rook = board.get(s.rookFrom);
                if (s.rook != null) {
                    s.rookOldSquare = s.rook.square;
                } else {
                    // Defensive: invalid castle state; keep rookFrom/To but rookPiece null
                    s.rookFrom = -1;
                    s.rookTo = -1;
                }
            }
        }

        // ---- APPLY ----

        // Clear from
        board.set(s.from, null);

        // Remove EP captured pawn
        if (s.epCapturedSq != -1) {
            board.set(s.epCapturedSq, null);
        }

        // Move rook for castling
        if (s.rookFrom != -1 && s.rook != null) {
            board.set(s.rookFrom, null);
            board.set(s.rookTo, s.rook);
            s.rook.square = s.rookTo;
        }

        // Promotion: place a temporary promoted piece on "to"
        if ((s.flags & Move.PROMO) != 0 && moving.getType() == PieceType.PAWN) {
            s.wasPromotion = true;
            PieceType promoType = PieceType.values()[Move.promo(move)];
            s.promotionPiece = new Piece(promoType, moving.getColor());

            board.set(s.to, s.promotionPiece);
            s.promotionPiece.square = s.to;
        } else {
            board.set(s.to, moving);
            moving.square = s.to;
        }

        return s;
    }

    public void undoMoveLite(MoveSnapshot s) {
        if (s == null || s.movedPiece == null) return;

        // Remove whatever we placed on "to" during apply
        if (s.wasPromotion) {
            // remove promoted piece from "to"
            board.set(s.to, null);
        } else {
            board.set(s.to, null);
        }

        // Undo castling rook
        if (s.rookFrom != -1 && s.rook != null) {
            board.set(s.rookTo, null);
            board.set(s.rookFrom, s.rook);
            s.rook.square = s.rookOldSquare;
        }

        // Restore en passant captured pawn
        if (s.epCapturedSq != -1) {
            board.set(s.epCapturedSq, s.epCapturedPiece);
        }

        // Restore captured piece on "to" (can be null)
        board.set(s.to, s.capturedOnTo);

        // Restore moved piece back to "from"
        board.set(s.from, s.movedPiece);
        s.movedPiece.square = s.movedOldSquare;
    }

    private void updateFiftyMoveRuleCounter(Piece piece, int flags) {
        if(piece.getType() == PieceType.PAWN || (flags & Move.CAPTURE) != 0) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
    }

    public int getHalfMoveCounter() {
        return halfMoveClock;
    }

    public void updateZobristKey(PieceColor sideToMove) {
        zobristKey = Zobrist.compute(this, sideToMove);
    }

    public long getZobristKey() {
        return zobristKey;
    }

    public int getFullMoves() {
        return fullMoves;
    }
}
