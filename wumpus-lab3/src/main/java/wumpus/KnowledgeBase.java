package wumpus;

import java.util.*;

public class KnowledgeBase {
    private final int size;

    // Память агента
    private final Set<Position> visited = new HashSet<>();
    private final Set<Position> safe = new HashSet<>();
    private final Set<Position> possiblePits = new HashSet<>();
    private final Set<Position> possibleWumpus = new HashSet<>();
    private Optional<Position> certainWumpus = Optional.empty();
    private boolean wumpusDead = false;

    public KnowledgeBase(int size) {
        this.size = size;
        // Стартовая клетка всегда безопасна и посещена
        Position start = new Position(1, 1);
        visited.add(start);
        safe.add(start);
    }

    /** Обновляет базу знаний на основе текущих восприятий */
    public void update(Position pos, Perception p) {
        visited.add(pos);
        safe.add(pos); // Если агент жив и находится здесь, значит клетка безопасна

        if (p.scream()) {
            wumpusDead = true;
            certainWumpus = Optional.empty();
            possibleWumpus.clear();
        }

        List<Position> neighbors = neighbors(pos);

        // 1. Логический вывод о ЯМАХ (Breeze)
        if (!p.breeze()) {
            // Нет ветерка -> ни один из соседей не является ямой
            for (Position n : neighbors) {
                safe.add(n);
                possiblePits.remove(n);
            }
        } else {
            // Есть ветерок -> как минимум один из соседей - яма
            for (Position n : neighbors) {
                if (!safe.contains(n)) {
                    possiblePits.add(n);
                }
            }
        }

        // 2. Логический вывод о ВАМПУСЕ (Stench)
        if (!wumpusDead) {
            if (!p.stench()) {
                // Нет запаха -> ни один из соседей не является Вампусом
                for (Position n : neighbors) {
                    safe.add(n);
                    possibleWumpus.remove(n);
                }
            } else {
                // Есть запах -> Вампус находится в одном из небезопасных соседей
                for (Position n : neighbors) {
                    if (!safe.contains(n)) {
                        possibleWumpus.add(n);
                    }
                }
            }
        }

        // 3. Уточнение: удаляем кандидатов, которые уже доказанно безопасны
        possiblePits.removeAll(safe);
        possibleWumpus.removeAll(safe);
        possibleWumpus.removeAll(possiblePits);

        // 4. Если осталось только одно возможное место для Вампуса, мы знаем его точное положение
        if (!wumpusDead && possibleWumpus.size() == 1) {
            certainWumpus = Optional.of(possibleWumpus.iterator().next());
        }
    }

    public boolean isSafe(Position p) {
        return safe.contains(p);
    }

    public boolean isVisited(Position p) {
        return visited.contains(p);
    }

    public Optional<Position> certainWumpus() {
        return certainWumpus;
    }

    public boolean wumpusDead() {
        return wumpusDead;
    }

    public Set<Position> visitedCells() {
        return visited;
    }

    public Set<Position> safeCells() {
        return safe;
    }

    /** Возвращает соседние клетки (вверх, вниз, влево, вправо) в пределах поля */
    public List<Position> neighbors(Position p) {
        List<Position> res = new ArrayList<>();
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            int nx = p.x() + dx[i];
            int ny = p.y() + dy[i];
            if (nx >= 1 && nx <= size && ny >= 1 && ny <= size) {
                res.add(new Position(nx, ny));
            }
        }
        return res;
    }
}