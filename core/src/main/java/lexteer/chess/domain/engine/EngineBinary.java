package lexteer.chess.domain.engine;

import lexteer.chess.util.Os;

import java.io.*;
import java.nio.file.*;

public final class EngineBinary {
    private EngineBinary() {}

    public static String prepareEnginePath() throws IOException {
        String resource = Os.isWindows()
            ? "/engine/stockfish/stockfish-windows.exe"
            : "/engine/stockfish/stockfish-linux";

        String suffix = Os.isWindows() ? ".exe" : "";
        Path out = Files.createTempFile("stockfish-", suffix);
        out.toFile().deleteOnExit();

        try (InputStream in = EngineBinary.class.getResourceAsStream(resource)) {
            if (in == null) throw new FileNotFoundException("Missing resource: " + resource);
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        }

        if (!Os.isWindows()) {
            // Make executable on Linux/macOS
            out.toFile().setExecutable(true);
        }

        return out.toAbsolutePath().toString();
    }
}

