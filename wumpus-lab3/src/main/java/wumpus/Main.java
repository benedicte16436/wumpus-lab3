package wumpus;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Размер поля N (N >= 4, по умолчанию 4): ");
        int n = 4;
        if (scanner.hasNextInt()) n = scanner.nextInt();

        System.out.print("Seed генерации (целое число, по умолчанию 42): ");
        long seed = 42;
        if (scanner.hasNextLong()) seed = scanner.nextLong();

        World world = new World(n, seed);
        HunterAgent agent = new HunterAgent(n);

        System.out.println("\nМир Вампуса: поле " + n + "x" + n + ". Агент не знает скрытую карту.\n");

        int maxSteps = n * n * 30;
        for (int step = 1; step <= maxSteps && !world.isDead() && !world.escaped(); step++) {
            Perception p = world.perceive();
            Action action = agent.decide(world.hunterPosition(), world.direction(), p);

            System.out.printf("Шаг %03d | позиция %s | направление %s | восприятие %s | действие %s%n",
                    step, world.hunterPosition(), world.direction(), p, action);

            world.act(action);

            if (world.isDead()) System.out.println("Охотник погиб!");
            if (world.escaped()) System.out.println("Охотник выбрался из пещеры.");
        }

        System.out.println("\n--- ИТОГИ ---");
        if (world.isDead()) {
            System.out.println("Результат: ПОРАЖЕНИЕ (агент погиб)");
        } else if (world.escaped()) {
            System.out.println("Результат: ПОБЕДА (агент выбрался)");
        } else {
            System.out.println("Результат: Лимит шагов исчерпан (агент застрял)");
        }

        System.out.println("Золото у агента: " + world.hasGold());
        System.out.println("Стрела осталась: " + world.arrowAvailable());
        System.out.println("Счет: " + world.score());
        System.out.println("Посещено клеток: " + agent.knowledgeBase().visitedCells().size());
        // CORRECTION ICI : parenthèse fermante ajoutée
        System.out.println("Известно безопасных клеток: " + agent.knowledgeBase().safeCells().size());

        System.out.println("\nСкрытая карта (для проверки):\n" + world.debugMap());

        scanner.close();
    }
}