package wumpus;

public enum Direction {
    NORTH(0, 1), EAST(1, 0), SOUTH(0, -1), WEST(-1, 0);
    final int dx, dy;
    Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
    public Direction left() { return values()[(ordinal() + 3) % 4]; }
    public Direction right() { return values()[(ordinal() + 1) % 4]; }
}
