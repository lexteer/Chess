package lexteer.chess.domain.board;

public enum File {
    A(0),
    B(1),
    C(2),
    D(3),
    E(4),
    F(5),
    G(6),
    H(7);

    private final int value;

    File(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static File getFileLetter(int value) {
        for(File file : File.values()) {
            if(file.value == value) {
                return file;
            }
        }
        throw new IllegalArgumentException("Invalid file value: " + value);
    }
}
