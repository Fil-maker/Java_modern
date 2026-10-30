package lesson3;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

/*
 * Задача «Резервная копия»
 *
 * Аргументы: исходный каталог (по умолчанию src) и расширение файлов (по умолчанию .java).
 * Скопируйте все такие файлы в каталог backup/<дата_время>, например
 * backup/2026-10-30_14-05-17, сохранив структуру подкаталогов.
 * Выведите, сколько файлов и байт скопировано и сколько это заняло времени.
 *
 * Пример: java lesson3.Backup src .java
 *   Копия: backup\2026-10-30_14-05-17
 *   Файлов: 21, байт: 48213
 *   Время: 37 мс
 */
public class Backup {
    // в именах файлов Windows нельзя использовать ':', поэтому часы через '-'
    static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static void main(String[] args) throws IOException {
        Path source = Path.of(args.length > 0 ? args[0] : "src");
        String ext = args.length > 1 ? args[1] : ".java";

        long start = System.nanoTime();
        Path target = Path.of("backup", LocalDateTime.now().format(STAMP));

        List<Path> files;
        try (Stream<Path> s = Files.walk(source)) {
            files = s.filter(Files::isRegularFile)
                     .filter(p -> p.getFileName().toString().endsWith(ext))
                     .toList();
        }

        long bytes = 0;
        for (Path p : files) {
            // src/lesson3/A.java -> backup/<время>/lesson3/A.java
            Path dest = target.resolve(source.relativize(p));
            Files.createDirectories(dest.getParent());
            Files.copy(p, dest, StandardCopyOption.COPY_ATTRIBUTES);
            bytes += Files.size(dest);
        }

        Duration took = Duration.ofNanos(System.nanoTime() - start);
        System.out.println("Копия: " + target);
        System.out.println("Файлов: " + files.size() + ", байт: " + bytes);
        System.out.println("Время: " + took.toMillis() + " мс");
    }
}
