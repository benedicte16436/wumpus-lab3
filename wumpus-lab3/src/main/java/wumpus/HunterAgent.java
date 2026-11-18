package wumpus;

import java.util.*;

/**
 * Агент, который выбирает действия на основании базы знаний, а не карты среды.
 */
public class HunterAgent {
    private final KnowledgeBase kb;
    private boolean hasGold = false;
    private boolean arrowUsed = false;
    private boolean escaped = false;

    // Атрибут для хранения текущего направления агента
    private Direction currentDirection = Direction.EAST;

    public HunterAgent(int size) {
        this.kb = new KnowledgeBase(size);
    }

    public KnowledgeBase knowledgeBase() {
        return kb;
    }

    public Action decide(Position position, Direction direction, Perception perception) {
        // 1. Обновить внутреннее направление агента данными из среды
        this.currentDirection = direction;

        // 2. Обновить базу знаний новыми восприятиями
        kb.update(position, perception);

        // 3. Подобрать золото, если оно видно
        if (perception.glitter() && !hasGold) {
            hasGold = true;
            return Action.GRAB;
        }

        // 4. Если золото есть, возвращаемся домой (1,1)
        if (hasGold) {
            if (position.equals(new Position(1,1))) {
                escaped = true;
                return Action.CLIMB;
            }
            return moveAlongPath(position, new Position(1,1));
        }

        // 5. Стрелять в Вампуса ТОЛЬКО если его положение точно известно и он на линии огня
        if (!arrowUsed && !kb.wumpusDead()) {
            Optional<Position> target = kb.certainWumpus();
            if (target.isPresent() && aligned(position, target.get())) {
                Direction desired = directionTo(position, target.get());
                if (currentDirection == desired) {
                    arrowUsed = true;
                    return Action.SHOOT;
                }
                return turnToward(desired);
            }
        }

        // 6. Исследовать новую безопасную непосещенную клетку
        Position target = nearestUnvisitedSafe(position);
        if (target != null) {
            return moveAlongPath(position, target);
        }

        // 7. Если больше нет безопасных клеток для исследования, осторожно возвращаемся
        if (position.equals(new Position(1,1))) {
            escaped = true;
            return Action.CLIMB;
        }
        return moveAlongPath(position, new Position(1,1));
    }

    /**
     * Поиск в ширину (BFS) для нахождения ближайшей безопасной непосещенной клетки
     */
    private Position nearestUnvisitedSafe(Position start) {
        Queue<Position> q = new ArrayDeque<>();
        Map<Position, Position> prev = new HashMap<>();
        q.add(start);
        prev.put(start, null);

        while (!q.isEmpty()) {
            Position p = q.poll();
            if (!p.equals(start) && kb.isSafe(p) && !kb.isVisited(p)) {
                return p;
            }
            for (Position n : kb.neighbors(p)) {
                if (kb.isSafe(n) && !prev.containsKey(n)) {
                    prev.put(n, p);
                    q.add(n);
                }
            }
        }
        return null;
    }

    /**
     * Движение по кратчайшему пути (BFS) к цели
     */
    private Action moveAlongPath(Position start, Position goal) {
        if (start.equals(goal)) {
            return Action.CLIMB; // Страховка, если уже на месте
        }

        Queue<Position> q = new ArrayDeque<>();
        Map<Position, Position> prev = new HashMap<>();
        q.add(start);
        prev.put(start, null);

        // Алгоритм BFS для поиска пути к цели
        while (!q.isEmpty() && !prev.containsKey(goal)) {
            Position p = q.poll();
            for (Position n : kb.neighbors(p)) {
                if (kb.isSafe(n) && !prev.containsKey(n)) {
                    prev.put(n, p);
                    q.add(n);
                }
            }
        }

        // Если путь не найден (не должно происходить при корректной БЗ)
        if (!prev.containsKey(goal)) {
            return start.equals(new Position(1,1)) ? Action.CLIMB : Action.FORWARD;
        }

        // Восстановление пути от цели к началу, чтобы найти первый шаг
        Position step = goal;
        while (prev.get(step) != null && !prev.get(step).equals(start)) {
            step = prev.get(step);
        }

        // Определение направления для первого шага
        Direction desired = directionTo(start, step);

        // Приказ агенту повернуться или двигаться вперед
        return turnToward(desired);
    }

    /**
     * Управление поворотом агента в нужном направлении.
     * Обновляет внутреннее направление агента, если требуется поворот.
     */
    private Action turnToward(Direction desired) {
        if (currentDirection == desired) {
            return Action.FORWARD; // Уже смотрим в нужную сторону - идем
        }

        if (currentDirection.right() == desired) {
            currentDirection = currentDirection.right(); // Обновление внутреннего направления
            return Action.TURN_RIGHT;
        }

        if (currentDirection.left() == desired) {
            currentDirection = currentDirection.left(); // Обновление внутреннего направления
            return Action.TURN_LEFT;
        }

        // Разворот на 180 градусов (например, с Востока на Запад): поворачиваем направо первый раз
        currentDirection = currentDirection.right();
        return Action.TURN_RIGHT;
    }

    /**
     * Проверяет, находятся ли две позиции на одной линии или колонке
     */
    private boolean aligned(Position a, Position b) {
        return a.x() == b.x() || a.y() == b.y();
    }

    /**
     * Вычисляет кардинальное направление от 'a' к 'b'
     */
    private Direction directionTo(Position a, Position b) {
        if (b.x() > a.x()) return Direction.EAST;
        if (b.x() < a.x()) return Direction.WEST;
        if (b.y() > a.y()) return Direction.NORTH;
        return Direction.SOUTH;
    }

    public boolean hasGold() { return hasGold; }
    public boolean escaped() { return escaped; }
}