package lexteer.chess.pieces;

import lexteer.chess.board.Board;
import lexteer.chess.board.BoardUi;
import lexteer.chess.main.Mouse;

public class PieceSelection {
    private final Mouse mouse;
    private final Board board;
    private final BoardUi boardUi;

    private Piece selectedPiece = null;

    private boolean validToMove = false;

    public PieceSelection(Mouse mouse, Board board, BoardUi boardUi) {
        this.mouse = mouse;
        this.board = board;
        this.boardUi = boardUi;
    }

    public void update() {
        // --- Phase 1: select (only once per press) ---
        if (mouse.pressed && selectedPiece == null) {
            trySelectPiece();
        }

        // --- Phase 2: while pressed (dragging or click-move) ---
        if (mouse.pressed) {
            handlePressed();
        }

        // --- Phase 3: release / reset ---
        if (!mouse.pressed) {
            handleRelease();
        }
    }

    private void trySelectPiece() {
        if (mouse.isOutsideTheBoard()) return;

        int mouseIndex = mouse.getIndex();
        Piece pieceAtMouse = board.get(mouseIndex);

        if (pieceAtMouse == null) return;

        selectedPiece = pieceAtMouse;
    }

    private void handlePressed() {
        // Keep the original behavior: if pressed outside board, do nothing else this frame.
        if (mouse.isOutsideTheBoard()) return;

        // IMPORTANT: your original code assumes selectedPiece is non-null by the time you drag/click.
        // Keeping the same assumption, but guarding avoids NPEs if something goes wrong.
        if (selectedPiece == null) return;

        if (mouse.dragging) {
            dragSelectedPiecePreview();
            updateValidToMoveFlag();
            return;
        }

        handleClickMoveOrCapture();
    }

    private void dragSelectedPiecePreview() {
        float squareSize = boardUi.getSquareSize();
        float mouseX = mouse.getX();
        float mouseY = mouse.getY();

        selectedPiece.updatePosition(mouseX - squareSize / 2f, mouseY - squareSize / 2f);
    }

    private void updateValidToMoveFlag() {
        int hoverIndex = mouse.getIndex();
        Piece pieceAtHover = board.get(hoverIndex);

        if (pieceAtHover == null) {
            validToMove = true;
            return;
        }

        if (pieceAtHover.getColor() != selectedPiece.getColor()) {
            validToMove = true;
        }

        if(mouse.isOutsideTheBoard()) {
            validToMove = false;
        }
    }

    private void handleClickMoveOrCapture() {
        int mouseIndex = mouse.getIndex();
        Piece pieceAtMouse = board.get(mouseIndex);

        if (pieceAtMouse != null) {
            if (pieceAtMouse.getColor() == selectedPiece.getColor()) {
                // Same-color click: switch selected piece (only if different square)
                if (mouseIndex != board.index(selectedPiece)) {
                    selectedPiece = pieceAtMouse;
                }
                return;
            }

            // Enemy piece: capture immediately
            movePiece(mouseIndex, selectedPiece);
            mouse.pressed = false;
            return;
        }

        // Empty square: move immediately
        movePiece(mouseIndex, selectedPiece);
        mouse.pressed = false;
    }

    private void handleRelease() {
        if (selectedPiece == null) return;

        int mouseIndex = mouse.getIndex();
        Piece pieceAtMouse = board.get(mouseIndex);

        if (validToMove && !mouse.isOutsideTheBoard() && mouseIndex != board.index(selectedPiece)) {
            if(pieceAtMouse != null){
                if(pieceAtMouse.getColor() != selectedPiece.getColor()) {
                    movePiece(mouseIndex, selectedPiece);
                    validToMove = false; // keep your reset
                    return;
                }
            } else {
                movePiece(mouseIndex, selectedPiece);
                validToMove = false; // keep your reset
                return;
            }
        }

        // Not moved: snap back
        board.set(board.index(selectedPiece), selectedPiece);
        validToMove = false;
    }


    public Piece getSelected() {
        return selectedPiece;
    }

    public void movePiece(int index, Piece piece) {
        board.set(index, piece);
        selectedPiece = null;
    }
}
