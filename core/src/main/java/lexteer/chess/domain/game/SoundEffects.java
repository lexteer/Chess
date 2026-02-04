package lexteer.chess.domain.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundEffects {

    Sound move, check, gameOver;

    public SoundEffects() {
        move = Gdx.audio.newSound(Gdx.files.internal("sounds/move-self.mp3"));
        check = Gdx.audio.newSound(Gdx.files.internal("sounds/move-check.mp3"));
        gameOver = Gdx.audio.newSound(Gdx.files.internal("sounds/game-end.mp3"));
    }

    public void playMove() {
        move.play(1f);
    }

    public void playCheck() {
        check.play(1f);
    }

    public void playGameOver() {
        gameOver.play(1f);
    }

    public void dispose() {
        move.dispose();
        check.dispose();
    }

}

