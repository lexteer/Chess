package lexteer.chess.ui.board;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import lexteer.chess.domain.board.File;

public class BoardCoords {
    GlyphLayout layout;
    FreeTypeFontGenerator gen;
    FreeTypeFontGenerator.FreeTypeFontParameter p;
    BitmapFont font;

    public BoardCoords() {
        gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto/static/Roboto-Regular.ttf"));
        layout = new GlyphLayout();

        p = new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 17;
        p.color = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;

        font = gen.generateFont(p);
        gen.dispose();
    }

    public void draw(SpriteBatch batch, BoardUi boardUi) {
        float square = boardUi.getSquareSize();
        float boardX = boardUi.getBoardX();
        float boardY = boardUi.getBoardY();

        // ranks
        for (int i = 1; i < 9; i++) {
            String rank = String.valueOf(i);
            layout.setText(font, rank);

            float textHeight = layout.height;
            float textWidth = layout.width;

            float y = boardY + square * (i - 1) + textHeight/2 + square/2;
            float x = boardX - textWidth - 11;

            font.draw(batch, rank, x, y);
        }

        // files
        int i = 0;
        for (File file : File.values()) {
            String letter = file.toString().toLowerCase();
            layout.setText(font, letter);

            float textWidth = layout.width;
            float x = boardX + square/2 - textWidth/2 + square * i;

            font.draw(batch, letter, x, boardY - 10);
            i++;
        }
    }
}
