package lesson4;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/*
 * Задача «Ведомость»
 *
 * Файл (аргумент командной строки, по умолчанию src/lesson4/grades.csv)
 * содержит строку заголовка и строки вида
 *   Фамилия;Группа;Предмет;Оценка          (оценка от 2 до 5)
 * Выведите:
 *   1) средний балл каждой группы (группы по алфавиту, два знака после запятой);
 *   2) лучшего студента каждой группы по среднему баллу
 *      (при равенстве — первого по алфавиту);
 *   3) отличников — студентов, у которых все оценки 5, по алфавиту;
 *   4) сколько выставлено пятёрок, четвёрок, троек и двоек;
 *   5) предмет с самым низким средним баллом.
 *
 * Пример вывода для приложенного grades.csv:
 *   Средний балл по группам:
 *     ИВТ-1  3,70
 *     ИВТ-2  4,19
 *     ПМИ-1  4,00
 *   Лучшие в группах:
 *     ИВТ-1  Сидоров (4,25)
 *     ИВТ-2  Петрова (5,00)
 *     ПМИ-1  Кузнецов (5,00)
 *   Отличники: Кузнецов, Петрова
 *   Распределение оценок:
 *     5: 17
 *     4: 22
 *     3: 14
 *     2: 3
 *   Самый трудный предмет: Матанализ (3,50)
 */
public class Gradebook {
    record Grade(String student, String group, String subject, int mark) {
        static Grade parse(String line) {
            String[] p = line.split(";");
            return new Grade(p[0].trim(), p[1].trim(), p[2].trim(), Integer.parseInt(p[3].trim()));
        }
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of(args.length > 0 ? args[0] : "src/lesson4/grades.csv");

        List<Grade> grades;
        try (Stream<String> lines = Files.lines(path)) {
            grades = lines.skip(1)                    // заголовок
                          .filter(l -> !l.isBlank())
                          .map(Grade::parse)
                          .toList();
        }

        // 1) группа → средний балл
        Map<String, Double> groupAvg = grades.stream()
                .collect(Collectors.groupingBy(Grade::group, TreeMap::new,
                        Collectors.averagingInt(Grade::mark)));
        System.out.println("Средний балл по группам:");
        groupAvg.forEach((g, avg) -> System.out.printf("  %-6s %.2f%n", g, avg));

        // 2) группа → (студент → средний), затем максимум внутри каждой группы
        Map<String, Map<String, Double>> studentAvg = grades.stream()
                .collect(Collectors.groupingBy(Grade::group, TreeMap::new,
                        Collectors.groupingBy(Grade::student, TreeMap::new,
                                Collectors.averagingInt(Grade::mark))));
        System.out.println("Лучшие в группах:");
        // больше средний — лучше; при равенстве «больше» тот, чья фамилия раньше по алфавиту
        Comparator<Map.Entry<String, Double>> better =
                Map.Entry.<String, Double>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder()));
        studentAvg.forEach((group, students) -> {
            Map.Entry<String, Double> best = students.entrySet().stream().max(better).orElseThrow();
            System.out.printf("  %-6s %s (%.2f)%n", group, best.getKey(), best.getValue());
        });

        // 3) отличники: все оценки студента равны 5
        List<String> excellent = grades.stream()
                .collect(Collectors.groupingBy(Grade::student, TreeMap::new,
                        Collectors.mapping(Grade::mark, Collectors.toList())))
                .entrySet().stream()
                .filter(e -> e.getValue().stream().allMatch(m -> m == 5))
                .map(Map.Entry::getKey)
                .toList();
        System.out.println("Отличники: " + (excellent.isEmpty() ? "нет" : String.join(", ", excellent)));

        // 4) оценка → количество, от 5 к 2
        Map<Integer, Long> byMark = grades.stream()
                .collect(Collectors.groupingBy(Grade::mark,
                        () -> new TreeMap<>(Comparator.reverseOrder()), Collectors.counting()));
        System.out.println("Распределение оценок:");
        for (int m = 5; m >= 2; m--) {
            System.out.println("  " + m + ": " + byMark.getOrDefault(m, 0L));
        }

        // 5) предмет с самым низким средним: Optional, потому что оценок может не быть
        grades.stream()
                .collect(Collectors.groupingBy(Grade::subject, Collectors.averagingInt(Grade::mark)))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .ifPresentOrElse(
                        e -> System.out.printf("Самый трудный предмет: %s (%.2f)%n", e.getKey(), e.getValue()),
                        () -> System.out.println("Оценок нет"));
    }
}
