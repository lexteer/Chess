package lexteer.chess.domain.move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RepetitionTracker {
    private final ArrayList<Long> history = new ArrayList<>(256);
    private final Map<Long, Integer> counts = new HashMap<>(512);

    public RepetitionTracker(long initialPosition) {
        reset();
        addPosition(initialPosition);
    }

    public void reset() {
        history.clear();
        counts.clear();
    }

    public void addPosition(long zobristKey) {
        history.add(zobristKey);
        counts.merge(zobristKey, 1, Integer::sum);
    }

    public void undoLastPosition() {
        if(history.isEmpty()) return;

        long key = history.remove(history.size() - 1);
        Integer c = counts.get(key);
        if(c == null) return;

        if(c <= 1) counts.remove(key);
        else counts.put(key, c -1);
    }

    public int getCount(long zobristKey) {
        return counts.getOrDefault(zobristKey, 0);
    }

    public boolean isThreeFold(long zobristKey) {
        return getCount(zobristKey) >= 3;
    }

}
