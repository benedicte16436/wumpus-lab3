package wumpus;

import java.util.*;

/** Скрытая среда. Агент получает только Perception, а не содержимое клеток. */
public class World {
    private final int size;
    private final Position hunterStart = new Position(1, 1);
    private final Position gold;
    private final Position wumpus;
    private final Set<Position> pits = new HashSet<>();
    private Position hunter = hunterStart;
    private Direction direction = Direction.EAST;
    private boolean wumpusAlive = true, arrowAvailable = true;
    private boolean hasGold = false, dead = false, escaped = false;
    private boolean bump = false, scream = false;
    private int score = 0;

    public World(int size, long seed) {
        if (size < 4) throw new IllegalArgumentException("Размер поля должен быть не меньше 4.");
        this.size = size;
        Random random = new Random(seed);
        List<Position> cells = new ArrayList<>();
        for (int x = 1; x <= size; x++) for (int y = 1; y <= size; y++)
            if (!(x == 1 && y == 1)) cells.add(new Position(x, y));
        Collections.shuffle(cells, random);
        wumpus = cells.get(0);
        gold = cells.get(1);
        // Вероятность ямы 0,2 в каждой комнате, кроме стартовой (как в описании среды).
        for (Position p : cells) if (random.nextDouble() < 0.20) pits.add(p);
    }

    public int size() { return size; }
    public Position hunterPosition() { return hunter; }
    public Direction direction() { return direction; }
    public boolean isDead() { return dead; }
    public boolean hasGold() { return hasGold; }
    public boolean escaped() { return escaped; }
    public int score() { return score; }
    public boolean arrowAvailable() { return arrowAvailable; }
    public boolean wumpusAlive() { return wumpusAlive; }

    public Perception perceive() {
        boolean stench = wumpusAlive && adjacent(hunter, wumpus);
        boolean breeze = pits.stream().anyMatch(p -> adjacent(hunter, p));
        boolean glitter = hunter.equals(gold) && !hasGold;
        return new Perception(stench, breeze, glitter, bump, scream);
    }

    public void act(Action action) {
        if (dead || escaped) return;
        score--;
        bump = false; scream = false;
        switch (action) {
            case TURN_LEFT -> direction = direction.left();
            case TURN_RIGHT -> direction = direction.right();
            case FORWARD -> moveForward();
            case SHOOT -> shoot();
            case GRAB -> { if (hunter.equals(gold) && !hasGold) { hasGold = true; score += 1000; } }
            case CLIMB -> { if (hunter.equals(hunterStart)) escaped = true; }
        }
    }

    private void moveForward() {
        Position next = new Position(hunter.x() + direction.dx, hunter.y() + direction.dy);
        if (!inside(next)) { bump = true; return; }
        hunter = next;
        if (pits.contains(hunter)) { dead = true; score -= 1000; }
        else if (wumpusAlive && hunter.equals(wumpus)) { dead = true; score -= 1000; }
    }

    private void shoot() {
        if (!arrowAvailable) return;
        arrowAvailable = false;
        score -= 10;
        Position p = hunter;
        while (true) {
            p = new Position(p.x() + direction.dx, p.y() + direction.dy);
            if (!inside(p)) return;
            if (wumpusAlive && p.equals(wumpus)) { wumpusAlive = false; scream = true; return; }
        }
    }

    public boolean inside(Position p) { return p.x() >= 1 && p.x() <= size && p.y() >= 1 && p.y() <= size; }
    public static boolean adjacent(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) == 1;
    }

    /** Только для визуализации преподавателю; агент не использует этот метод. */
    public String debugMap() {
        StringBuilder out = new StringBuilder();
        for (int y = size; y >= 1; y--) {
            for (int x = 1; x <= size; x++) {
                Position p = new Position(x, y);
                char c = p.equals(hunterStart) ? 'S' : '.';
                if (pits.contains(p)) c = 'P';
                if (p.equals(gold)) c = 'G';
                if (p.equals(wumpus)) c = 'W';
                out.append(c).append(' ');
            }
            out.append('\n');
        }
        return out.toString();
    }
}
