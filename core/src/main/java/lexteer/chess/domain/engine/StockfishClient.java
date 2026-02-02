package lexteer.chess.domain.engine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public final class StockfishClient implements Closeable {
    private final Process process;
    private final BufferedWriter stdin;
    private final BufferedReader stdout;

    private final ExecutorService readerThread = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "stockfish-stdout");
        t.setDaemon(true);
        return t;
    });

    private final AtomicReference<CompletableFuture<String>> pendingBestMove = new AtomicReference<>();

    public StockfishClient(String enginePath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(enginePath);
        pb.redirectErrorStream(true);
        this.process = pb.start();

        this.stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        this.stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        uciHandshake();
        configureStrength(5, 1400);
        startReaderLoop();

    }

    private void startReaderLoop() {
        readerThread.submit(() -> {
            try {
                String line;
                while ((line = stdout.readLine()) != null) {
                    // System.out.println("[SF] " + line);

                    if (line.startsWith("bestmove ")) {
                        String move = line.substring("bestmove ".length()).trim();
                        int space = move.indexOf(' ');
                        if (space >= 0) move = move.substring(0, space);

                        CompletableFuture<String> fut = pendingBestMove.getAndSet(null);
                        if (fut != null && !fut.isDone()) fut.complete(move);
                    }
                }
            } catch (IOException ex) {
                CompletableFuture<String> fut = pendingBestMove.getAndSet(null);
                if (fut != null && !fut.isDone()) fut.completeExceptionally(ex);
            }
        });
    }

    private void uciHandshake() throws IOException {
        send("uci");
        // Wait until "uciok"
        waitForLine("uciok", 2, TimeUnit.SECONDS);

        send("isready");
        waitForLine("readyok", 2, TimeUnit.SECONDS);

        // Optional: tune options here
        // send("setoption name Threads value 2");
        // send("setoption name Hash value 128");
        // send("ucinewgame");
        // send("isready");
        // waitForLine("readyok", 2, TimeUnit.SECONDS);
    }

    private void waitForLine(String expected, long timeout, TimeUnit unit) throws IOException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            if (!stdout.ready()) {
                try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                continue;
            }
            String line = stdout.readLine();
            if (line == null) throw new EOFException("Stockfish terminated.");
            if (line.trim().equals(expected)) return;

            // also route bestmove lines to pending request if they appear unexpectedly
            if (line.startsWith("bestmove ")) {
                String move = line.substring("bestmove ".length()).trim();
                int space = move.indexOf(' ');
                if (space >= 0) move = move.substring(0, space);
                CompletableFuture<String> fut = pendingBestMove.getAndSet(null);
                if (fut != null && !fut.isDone()) fut.complete(move);
            }
        }
        throw new IOException("Timeout waiting for: " + expected);
    }

    private synchronized void send(String cmd) throws IOException {
        stdin.write(cmd);
        stdin.write('\n');
        stdin.flush();
    }

    /**
     * Ask Stockfish to compute a move for the given FEN.
     *
     * @param fen current position in FEN
     * @param movetimeMs engine think time in milliseconds
     * @return future that completes with UCI bestmove (e.g., "e2e4", "e7e8q")
     */
    public CompletableFuture<String> getBestMoveFromFenAsync(String fen, int movetimeMs) {
        CompletableFuture<String> fut = new CompletableFuture<>();
        CompletableFuture<String> prev = pendingBestMove.getAndSet(fut);
        if (prev != null && !prev.isDone()) {
            prev.completeExceptionally(new IllegalStateException("Overlapping getBestMove requests"));
        }

        try {
            send("position fen " + fen);
            send("go movetime " + movetimeMs);
        } catch (IOException ex) {
            pendingBestMove.compareAndSet(fut, null);
            fut.completeExceptionally(ex);
        }
        return fut;
    }

    public void configureStrength(int skillLevel, Integer eloOrNull) throws IOException {
        send("setoption name Skill Level value " + skillLevel);

        if (eloOrNull != null) {
            send("setoption name UCI_LimitStrength value true");
            send("setoption name UCI_Elo value " + eloOrNull);
        } else {
            send("setoption name UCI_LimitStrength value false");
        }

        send("isready");
        waitForLine("readyok", 2, TimeUnit.SECONDS);
    }


    @Override
    public void close() throws IOException {
        try {
            send("quit");
        } catch (IOException ignored) {
        }
        process.destroy();
        readerThread.shutdownNow();
    }
}

