package lexteer.chess.main;

import lexteer.chess.main.enums.PieceType;

public final class Move {
    // flags
    public static final int CAPTURE   = 1 << 12;
    public static final int ENPASSANT = 1 << 13;
    public static final int CASTLE    = 1 << 14;
    public static final int PROMO     = 1 << 15;

    private Move() {}

    public static int make(int from, int to) {
        return from | (to << 6);
    }

    public static int make(int from, int to, int flags, int promo) {
        return from | (to << 6) | flags | (promo << 16);
    }

    public static int makeCapture(int from, int to) {
        return from | (to << 6) | CAPTURE;
    }

    public static int from(int mv) {
        return mv & 63;
    }

    public static int to(int mv) {
        return (mv >>> 6) & 63;
    }

    public static int flags(int mv) {
        return mv & (0xF << 12);
    }

    public static int promo(int mv) {
        return (mv >>> 16) & 0xF;
    }

    public static int withPromo(int mv, int promo) {
        // clear bits 16..19, then set them
        mv = mv & ~(0xF << 16);
        mv |= (promo & 0xF) << 16;
        // ensure PROMO flag is set
        mv |= PROMO;
        return mv;
    }


    public static boolean isCapture(int mv) {
        return (mv & CAPTURE) != 0;
    }
}


