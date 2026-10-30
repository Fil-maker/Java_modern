package lesson3;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

/*
 * Задача «Недавно изменённые файлы»
 *
 * Аргументы: каталог (по умолчанию src) и число дней d (по умолчанию 7).
 * Найдите во всём дереве каталога файлы, изменённые за последние d дней,
 * и выведите их от самых свежих к старым в формате
 *   дата изменения   размер   путь относительно каталога
 * В конце — количество найденных файлов.
 *
 * Пример: java lesson3.RecentFiles src 3
 *   2026-10-30 21:28   104259 B  lesson3\DateTime_Files_presentation.pptx
 *   2026-10-30 14:01     2647 B  lesson3\ListIO.java
 *   Найдено файлов: 2
 */
public class RecentFiles {
    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    record Entry(Path path, Instant modified, long size) {}

    public static void main(String[] args) throws IOException {
        Path dir = Path.of(args.length > 0 ? args[0] : "src");
        int days = args.length > 1 ? Integer.parseInt(args[1]) : 7;

        // граница — момент времени, поэтому Instant и Duration
        Instant border = Instant.now().minus(Duration.ofDays(days));

        List<Entry> recent;
        // walk тоже держит каталоги открытыми — try-with-resources
        try (Stream<Path> s = Files.walk(dir)) {
            recent = s.filter(Files::isRegularFile)
                      .map(RecentFiles::entry)
                      .filter(e -> e.modified().isAfter(border))
                      .sorted(Comparator.comparing(Entry::modified).reversed())
                      .toList();
        }

        ZoneId zone = ZoneId.systemDefault();
        StringBuilder sb = new StringBuilder();
        for (Entry e : recent) {
            // Instant -> местное время для показа пользователю
            LocalDateTime t = LocalDateTime.ofInstant(e.modified(), zone);
            sb.append(String.format("%s %8d B  %s%n", t.format(F), e.size(), dir.relativize(e.path())));
        }
        sb.append("Найдено файлов: ").append(recent.size());
        System.out.println(sb);
    }

    // лямбды в Stream не могут бросать IOException — заворачиваем в UncheckedIOException
    static Entry entry(Path p) {
        try {
            return new Entry(p, Files.getLastModifiedTime(p).toInstant(), Files.size(p));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
