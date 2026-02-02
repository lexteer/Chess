package lexteer.chess.domain.engine;

import com.badlogic.gdx.Gdx;
import lexteer.chess.domain.board.FEN;
import lexteer.chess.domain.game.GameState;
import lexteer.chess.domain.piece.PieceColor;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EngineController implements Closeable {
    private final StockfishClient stockfish;
    private final AtomicBoolean busy = new AtomicBoolean(false);

    public EngineController(String enginePath) throws Exception {
        this.stockfish = new StockfishClient(enginePath);
    }

    /**
     * Requests and applies the engine move.
     *
     * Contract (as you requested):
     * - You call this only when game is not finished and it is engine's turn.
     * - Any "no move" result is treated as an error.
     * - One request at a time.
     */
    public void requestAndApplyMove(GameState state, PieceColor sideToMove, int movetimeMs, Runnable afterApply) {
        if (!busy.compareAndSet(false, true)) {
            throw new IllegalStateException("Engine request already in progress.");
        }

        final String fen = FEN.generate(state, sideToMove);

        CompletableFuture<String> bestMoveFuture = stockfish.getBestMoveFromFenAsync(fen, movetimeMs);

        bestMoveFuture
            .thenApply(uci -> UciToMove.toMoveInt(uci, state))
            .thenAccept(encodedMove -> Gdx.app.postRunnable(() -> {
                try {
                    state.applyMove(encodedMove);
                    if (afterApply != null) afterApply.run();
                } finally {
                    busy.set(false);
                }
            }))
            .exceptionally(ex -> {
                busy.set(false);
                throw new RuntimeException("Stockfish failed", ex);
            });

    }

    @Override
    public void close() {
        try {
            stockfish.close();
        } catch (Exception e) {
            // fail silently on shutdown
        }
    }

}

