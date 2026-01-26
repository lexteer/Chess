package lexteer.chess.main;

import lexteer.chess.board.Board;
import lexteer.chess.board.BoardUi;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.pieces.Piece;

public class SelectionMoving {
    private final Mouse mouse;
    private final Board board;
    private final BoardUi boardUi;
    private final GameScreen gameScreen;

    private Piece selectedPiece = null;

    // helpers
    private boolean reselected = false;
    private boolean pickUpPiece = false;

    public SelectionMoving(Mouse mouse, Board board, BoardUi boardUi, GameScreen gameScreen) {
        this.mouse = mouse;
        this.board = board;
        this.boardUi = boardUi;
        this.gameScreen = gameScreen;
    }

    public void update(PieceColor currentColor) {
        int mouseIndex = mouse.getIndex();
        Piece mousePiece = board.get(mouseIndex);
        boolean mousePressed = mouse.pressed;
        boolean mouseDragging = mouse.dragging;

        selectNewPiece(mouseIndex, mousePiece, currentColor);
        selectedFollowMouse(mouseIndex, mousePiece, mousePressed, mouseDragging);
        clickMovePiece(mouseIndex, mousePiece, mousePressed, mouseDragging);
    }

    // select, reselect
    private void selectNewPiece(int mouseIndex, Piece mousePiece, PieceColor currentColor) {
        if (mouse.justPressed){
            if(mouseIndex == -1) return;
            if(mousePiece == null) return;
            if(mousePiece.getColor() != currentColor) return;

            if (selectedPiece == null) {
                selectedPiece = mousePiece;
            }
            // reselect
            else if(selectedPiece.getColor() == mousePiece.getColor() && !reselected){
                selectedPiece = mousePiece;
                reselected = true;
            }
        } else {
            reselected = false;
        }
    }

    private void selectedFollowMouse(int mouseIndex, Piece mousePiece, boolean mousePressed, boolean mouseDragging) {
        if (mouseDragging && selectedPiece != null) {
            if (mouseIndex == board.index(selectedPiece)) {
                pickUpPiece = true;
            }
        }
        if (mouseDragging && selectedPiece != null && pickUpPiece) {
            float squareSize = boardUi.getSquareSize();
            float x = mouse.getX() - (squareSize / 2);
            float y = mouse.getY() - (squareSize / 2);

            x = clampX(x);
            y = clampY(y);

            selectedPiece.updatePosition(x, y);
        }

        if(!mousePressed) {
            dragMovePiece(mouseIndex, mousePiece, pickUpPiece);
            pickUpPiece = false;
        }
    }

    private void clickMovePiece(int mouseIndex, Piece mousePiece, boolean mousePressed, boolean mouseDragging) {
        if(selectedPiece == null) return;
        if(mouseIndex == -1) {
            // deselect of clicked outside the board
            if(mousePressed && !mouseDragging) {
                selectedPiece = null;
            }
            return;
        }

        if(mousePressed && !mouseDragging) {
            if(mouseIndex == board.index(selectedPiece)) return;

            if(mousePiece == null || mousePiece.getColor() != selectedPiece.getColor()) {
                movePiece(mouseIndex, selectedPiece);
            }
        }
    }

    private void dragMovePiece(int mouseIndex, Piece mousePiece, boolean pickUpPiece) {
        if(selectedPiece == null) return;

        if(mouseIndex == -1 && pickUpPiece) {
            resetSelectedPiecePos();
            selectedPiece = null;
            return;
        }

        if(mousePiece == null || mousePiece.getColor() != selectedPiece.getColor()) {
            if(pickUpPiece) {
                movePiece(mouseIndex, selectedPiece);
            }
        } else {
            resetSelectedPiecePos();
        }
    }

    private void resetSelectedPiecePos() {
        board.set(board.index(selectedPiece), selectedPiece);
    }

    public Piece getSelected() {
        return selectedPiece;
    }

    public void movePiece(int index, Piece piece) {
        board.set(index, piece);
        selectedPiece = null;
        mouse.pressed = false;
        gameScreen.switchPlayer();
    }

    // keeps the dragged piece inside the board
    private float clampX(float x) {
        float boardX = boardUi.getBoardX();
        float boardSize = boardUi.getBoardSize();

        float minX = boardX - boardUi.getSquareSize()/2;
        float maxX = boardX + boardSize - boardUi.getSquareSize()/2;

        if(x < minX) return minX;
        if(x > maxX) return maxX;

        return x;
    }

    private float clampY(float y) {
        float boardY = boardUi.getBoardY();
        float boardSize = boardUi.getBoardSize();

        float minY = boardY - boardUi.getSquareSize()/2;
        float maxY = boardY + boardSize - boardUi.getSquareSize()/2;

        if(y < minY) return minY;
        if(y > maxY) return maxY;

        return y;
    }
}
