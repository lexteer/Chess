package lexteer.chess.util;

public class Os {
    private Os() {}

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isLinux() {
        String n = System.getProperty("os.name").toLowerCase();
        return n.contains("nux") || n.contains("linux");
    }
}
