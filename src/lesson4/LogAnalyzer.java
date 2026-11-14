package lesson4;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

/*
 * Задача «Анализ лога сервера»
 *
 * Файл (аргумент командной строки, по умолчанию src/lesson4/server.log)
 * состоит из строк вида
 *   2026-11-14T09:15:02 ERROR auth Неверный пароль для user42
 * то есть: момент времени (ISO-8601), уровень (ERROR, WARN, INFO, DEBUG),
 * модуль и текст сообщения. Строки, которые не удалось разобрать, пропускаются
 * и подсчитываются отдельно.
 * Выведите:
 *   1) количество записей каждого уровня (от ERROR к DEBUG, нулевые тоже);
 *   2) три модуля с наибольшим числом ошибок ERROR (при равенстве — по алфавиту);
 *   3) гистограмму ошибок ERROR по часам: «09:00  ###  3»;
 *   4) время первой и последней ошибки и сколько прошло между ними;
 *   5) число нераспознанных строк.
 *
 * Пример вывода для приложенного server.log:
 *   Записей по уровням:
 *     ERROR 19
 *     WARN  16
 *     INFO  70
 *     DEBUG 13
 *   Модули с наибольшим числом ошибок:
 *     auth — 6
 *     db — 5
 *     api — 4
 *   Ошибки по часам:
 *     09:00  ##         2
 *     10:00  #########  9
 *     11:00  #####      5
 *     12:00  ###        3
 *   Первая ошибка: 2026-11-14T09:43:06
 *   Последняя:     2026-11-14T12:49:53
 *   Между ними: 3 ч 6 мин
 *   Нераспознанных строк: 3
 */
public class LogAnalyzer {
    // Порядок констант enum задаёт порядок ключей в EnumMap
    enum Level { ERROR, WARN, INFO, DEBUG }

    record Entry(LocalDateTime time, Level level, String module, String message) {}

    public static void main(String[] args) throws IOException {
        Path path = Path.of(args.length > 0 ? args[0] : "src/lesson4/server.log");
        List<String> lines = Files.readAllLines(path);

        List<Entry> entries = lines.stream()
                .map(LogAnalyzer::parse)
                .flatMap(Optional::stream)          // пустые Optional выпадают из потока
                .toList();
        long broken = lines.stream().filter(l -> !l.isBlank()).count() - entries.size();

        // 1) EnumMap — самая быстрая Map для ключей-enum, порядок как в объявлении
        Map<Level, Long> byLevel = new EnumMap<>(Level.class);
        for (Level l : Level.values()) byLevel.put(l, 0L);
        entries.forEach(e -> byLevel.merge(e.level(), 1L, Long::sum));
        System.out.println("Записей по уровням:");
        byLevel.forEach((l, n) -> System.out.printf("  %-5s %d%n", l, n));

        List<Entry> errors = entries.stream().filter(e -> e.level() == Level.ERROR).toList();

        // 2) топ-3 модулей по ошибкам
        System.out.println("Модули с наибольшим числом ошибок:");
        errors.stream()
                .collect(Collectors.groupingBy(Entry::module, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(3)
                .forEach(e -> System.out.println("  " + e.getKey() + " — " + e.getValue()));

        // 3) гистограмма по часам: TreeMap — часы по порядку
        Map<Integer, Long> byHour = errors.stream()
                .collect(Collectors.groupingBy(e -> e.time().getHour(), TreeMap::new, Collectors.counting()));
        System.out.println("Ошибки по часам:");
        byHour.forEach((h, n) -> System.out.printf("  %02d:00  %-10s %d%n", h, "#".repeat(n.intValue()), n));

        // 4) первая и последняя ошибка — min/max возвращают Optional
        Optional<LocalDateTime> first = errors.stream().map(Entry::time).min(Comparator.naturalOrder());
        Optional<LocalDateTime> last = errors.stream().map(Entry::time).max(Comparator.naturalOrder());
        if (first.isPresent()) {
            Duration d = Duration.between(first.get(), last.get());
            System.out.println("Первая ошибка: " + first.get());
            System.out.println("Последняя:     " + last.get());
            System.out.printf("Между ними: %d ч %d мин%n", d.toHours(), d.toMinutesPart());
        } else {
            System.out.println("Ошибок нет");
        }

        // 5)
        System.out.println("Нераспознанных строк: " + broken);
    }

    // Разбор одной строки. Optional.empty() вместо null или исключения наружу
    static Optional<Entry> parse(String line) {
        String[] p = line.trim().split("\\s+", 4);
        if (p.length < 4) return Optional.empty();
        try {
            return Optional.of(new Entry(LocalDateTime.parse(p[0]), Level.valueOf(p[1]), p[2], p[3]));
        } catch (DateTimeException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
