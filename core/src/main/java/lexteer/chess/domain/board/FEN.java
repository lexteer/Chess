package lexteer.chess.domain.board;

import lexteer.chess.domain.game.GameState;
import lexteer.chess.domain.piece.Piece;
import lexteer.chess.domain.piece.PieceColor;
import lexteer.chess.domain.piece.PieceType;

import java.util.ArrayList;

public class FEN {

    public static String generate(GameState state, PieceColor sideToMove) {
        Board board = state.board;
        StringBuilder key = new StringBuilder();

        key.append(piecePlacement(board));
        key.append(" ");
        key.append(activeColor(sideToMove));
        key.append(" ");
        key.append(castlingCheck(board));
        key.append(" ");
        key.append(enPassantSquare(state, sideToMove));
        key.append(" ");
        key.append(state.getHalfMoveCounter());
        key.append(" ");
        key.append(state.getFullMoves());

        return key.toString();
    }

    private static String piecePlacement(Board board) {
        StringBuilder key = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                Piece sqPiece = board.get(rank * 8 + file);

                if (sqPiece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount != 0) {
                        key.append(emptyCount);
                        emptyCount = 0;
                    }
                    key.append(getPieceLetter(sqPiece));
                }

                if (file == 7 && emptyCount != 0) {
                    key.append(emptyCount);
                }
            }
            if (rank > 0) key.append("/");
        }

        return key.toString();
    }

    private static String getPieceLetter(Piece piece) {
        if (piece == null) return "";

        PieceColor color = piece.getColor();

        String letter = switch (piece.getType()) {
            case PAWN -> "P";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case QUEEN -> "Q";
            case KING -> "K";
        };

        if (color == PieceColor.BLACK) {
            return letter.toLowerCase();
        } else {
            return letter;
        }
    }

    private static String activeColor(PieceColor color) {
        return (color == PieceColor.WHITE) ? "w" : "b";
    }

    private static String castlingCheck(Board board) {
        StringBuilder key = new StringBuilder();

        // white
        Piece whiteKing = board.get(4);
        if(Zobrist.isKingUnmoved(whiteKing, PieceColor.WHITE)) {
            Piece wR_kingSide = board.get(7);
            if (Zobrist.isRookUnmoved(wR_kingSide, PieceColor.WHITE)) {
                key.append("K");
            }

            Piece wR_queenSide = board.get(0);
            if (Zobrist.isRookUnmoved(wR_queenSide, PieceColor.WHITE)) {
                key.append("Q");
            }
        }

        // black
        Piece blackKing = board.get(60);
        if(Zobrist.isKingUnmoved(blackKing, PieceColor.BLACK)) {
            Piece bR_kingSide = board.get(63);
            if (Zobrist.isRookUnmoved(bR_kingSide, PieceColor.BLACK)) {
                key.append("k");
            }

            Piece bR_queenSide = board.get(56);
            if (Zobrist.isRookUnmoved(bR_queenSide, PieceColor.BLACK)) {
                key.append("q");
            }
        }

        if (key.isEmpty()) {
            return "-";
        } else {
            return key.toString();
        }
    }

    // returns a notation style square of the en passant target
    private static String enPassantSquare(GameState state, PieceColor sideToMove) {
        int ep = state.enPassantSquare;
        if (ep == -1) return "-";

        return sqIndexToNotation(ep);
    }

    private static String sqIndexToNotation(int sq) {
        int rank = Board.getRank(sq) + 1;
        String file = Board.getFile(sq).name().toLowerCase();

        return file + rank;
    }

    // METHODS FOR LOAD FEN INTO BOARD POSITION
    public static void load(String fen, GameState state) {
        Board board = state.board;

        String[] parts = fen.split(" ");

        String[] piecePositionRows = parts[0].split("/");

        // PIECE POSITION
        int row = 7;
        for (String part : piecePositionRows) {
            int file = 0;
            for (int i = 0; i < part.length(); i++) {
                char symbol = part.charAt(i);

                if (Character.isDigit(symbol)) {
                    file += symbol;
                    continue;
                } else {
                    int index = Board.index(Board.getFile(file), row);
                    PieceColor color = (Character.isUpperCase(symbol)) ? PieceColor.WHITE : PieceColor.BLACK;
                    PieceType type = getTypeFromLetter(symbol);

                    board.initPiece(index, type, color);
                }
                file++;
            }
            row--;
        }

        // SIDE TO MOVE
        PieceColor sideToMove = (parts[1].equals("w")) ? PieceColor.WHITE : PieceColor.BLACK;
        state.gameScreen.setCurrentPlaying(sideToMove);

        // CASTLING RIGHTS
        boolean wK = false, wQ = false, bK = false, bQ = false;
        String castlingRights = parts[2];

        if (castlingRights.equals("-")) {
            wK = false;
            wQ = false;
            bK = false;
            bQ = false;
        } else {
            for (int i = 0; i < castlingRights.length(); i++) {
                char letter = castlingRights.charAt(i);

                if (letter == 'K') {
                    wK = true;
                }

                if (letter == 'Q') {
                    wQ = true;
                }

                if (letter == 'k') {
                    bK = true;
                }

                if (letter == 'q') {
                    bQ = true;
                }
            }
        }

        Piece wKing = board.getKing(PieceColor.WHITE);
        Piece bKing = board.getKing(PieceColor.BLACK);

        // king moved
        if (!wK && !wQ) {
            wKing.pieceMoved();
        }

        if (!bK && !bQ) {
            bKing.pieceMoved();
        }

        // rook moved
        Piece wRookA1 = board.get(0);
        Piece wRookH1 = board.get(7);
        Piece bRookA8 = board.get(56);
        Piece bRookH8 = board.get(63);

        // White rooks
        if (wRookH1 != null && wRookH1.getType() == PieceType.ROOK && wRookH1.getColor() == PieceColor.WHITE) {
            if (!wK) wRookH1.pieceMoved();
        }
        if (wRookA1 != null && wRookA1.getType() == PieceType.ROOK && wRookA1.getColor() == PieceColor.WHITE) {
            if (!wQ) wRookA1.pieceMoved();
        }

        // Black rooks
        if (bRookH8 != null && bRookH8.getType() == PieceType.ROOK && bRookH8.getColor() == PieceColor.BLACK) {
            if (!bK) bRookH8.pieceMoved();
        }
        if (bRookA8 != null && bRookA8.getType() == PieceType.ROOK && bRookA8.getColor() == PieceColor.BLACK) {
            if (!bQ) bRookA8.pieceMoved();
        }

        // EN PASSANT SQUARE
        if (!parts[3].equals("-")) {
            String enSq = parts[3];
            char f = enSq.charAt(0);
            char r = enSq.charAt(1);

            File file = File.getFileLetter(getFileIndex(f));
            int rank = Character.getNumericValue(r - 1);

            state.enPassantSquare = Board.index(file, rank);
        }

        // HALF MOVE COUNTER
        String halfCounter = parts[4];
        state.halfMoveClock = Integer.parseInt(halfCounter);

        // FULL MOVE COUNTER
        String fullCounter = parts[5];
        state.fullMoves = Integer.parseInt(fullCounter);
    }

    private static PieceType getTypeFromLetter(char letter) {
        return switch (letter) {
            case 'p', 'P' -> PieceType.PAWN;
            case 'n', 'N' -> PieceType.KNIGHT;
            case 'b', 'B' -> PieceType.BISHOP;
            case 'r', 'R' -> PieceType.ROOK;
            case 'q', 'Q' -> PieceType.QUEEN;
            case 'k', 'K' -> PieceType.KING;
            default -> null;
        };
    }

    private static int getFileIndex(char letter) {
        return switch (letter) {
            case 'a' -> 0;
            case 'b' -> 1;
            case 'c' -> 2;
            case 'd' -> 3;
            case 'e' -> 4;
            case 'f' -> 5;
            case 'g' -> 6;
            case 'h' -> 7;
            default -> -1;
        };
    }
}
