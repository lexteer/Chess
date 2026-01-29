package lexteer.chess.board;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import lexteer.chess.assets.Assets;
import lexteer.chess.main.Mouse;
import lexteer.chess.main.enums.PieceColor;
import lexteer.chess.main.enums.PieceType;

import java.util.function.Consumer;


public class PromotionGUI {

    public PromotionGUI() {}

    private static boolean open = false;
    private static PieceColor color;
    private static Consumer<PieceType> callback;

    private static BoardUi boardUi;
    private static int targetIndex;

    private static final Rectangle[] rects = new Rectangle[] {
        new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()
    };

    private static final PieceType[] types = new PieceType[] {
        PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    private static Texture dimTex;
    private static Texture whiteTex;

    public static boolean isOpen() {
        return open;
    }

    public static void open(BoardUi ui, int promoTargetIndex, PieceColor promoColor, Consumer<PieceType> onChosen) {
        boardUi = ui;
        targetIndex = promoTargetIndex;
        color = promoColor;
        callback = onChosen;

        helperTextures();
        computeRects();

        open = true;
    }

    public static void close() {
        open = false;
        callback = null;
        boardUi = null;
    }

    public static boolean update(Mouse mouse) {
        if (!open) return false;

        if(!mouse.justPressed) return true;

        float mx = mouse.getX();
        float my = mouse.getY();

        for (int i = 0; i < rects.length; i++) {
            if (rects[i].contains(mx, my)) {
                final Consumer<PieceType> cb = callback;
                final PieceType chosen = types[i];
                close();
                Gdx.app.postRunnable(() -> {
                    if (cb != null) cb.accept(chosen);
                });

                return true;
            }
        }

        return true;
    }

    public static void draw(Batch batch) {
        if(!open || boardUi == null) return;

        // dim the whole screen
        batch.setColor(0f, 0f, 0f, 0.55f);
        float screenWidth = boardUi.camera.viewportWidth;
        float screenHeight = boardUi.camera.viewportHeight;
        batch.draw(dimTex, 0, 0, screenWidth, screenHeight);

        // draw white background under pieces
        batch.setColor(1f, 1f, 1f, 1f);

        for(int i = 0; i < rects.length; i++) {
            Rectangle rect = rects[i];

            // white bg
            batch.draw(whiteTex, rect.x, rect.y, rect.width, rect.height);

            // piece icon
            Texture pieceTex = getPieceTexture(color, types[i]);
            batch.draw(pieceTex, rect.x, rect.y, rect.width, rect.height);
        }

    }

    private static void computeRects() {
        float squareSize = boardUi.getSquareSize();

        int file = targetIndex & 7;
        int rank = targetIndex >>> 3;

        float baseX = boardUi.getBoardX() + file * squareSize;
        float baseY = boardUi.getBoardY() + rank * squareSize;

        // queen on target square
        rects[0].set(baseX, baseY, squareSize, squareSize);

        float dir = (color == PieceColor.WHITE) ? -1f : 1f;

        rects[1].set(baseX, baseY + dir * 1f * squareSize, squareSize, squareSize);
        rects[2].set(baseX, baseY + dir * 2f * squareSize, squareSize, squareSize);
        rects[3].set(baseX, baseY + dir * 3f * squareSize, squareSize, squareSize);
    }

    private static Texture getPieceTexture(PieceColor color, PieceType type) {
        String col = (color == PieceColor.WHITE) ? "white" : "black";
        String t = type.name().toLowerCase();
        String path = "pieces/" + col + "/" + t + ".png";
        return Assets.get(path, Texture.class);
    }

    private static void helperTextures() {
        if(dimTex != null && whiteTex != null) return;

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fill();
        whiteTex = new Texture(pm);

        dimTex = whiteTex;

        pm.dispose();
    }
}
