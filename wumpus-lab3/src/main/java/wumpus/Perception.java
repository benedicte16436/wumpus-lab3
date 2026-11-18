package wumpus;

/** Пять показаний датчиков агента. */
public record Perception(boolean stench, boolean breeze, boolean glitter,
                         boolean bump, boolean scream) {
    @Override public String toString() {
        return "[Stench=" + stench + ", Breeze=" + breeze + ", Glitter=" + glitter
                + ", Bump=" + bump + ", Scream=" + scream + "]";
    }
}
